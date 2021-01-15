package spl.interpreter.splObjects;

import spl.ast.NameNode;
import spl.ast.Node;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.*;
import spl.interpreter.splErrors.NativeTypeError;
import spl.util.Constants;
import spl.util.LineFilePos;

public class SplArray extends SplObject {

    public final int length;

    /**
     * Element type representation, can be found in static final content of {@code SplElement}
     */
    public final int elementTypeCode;

    public SplArray(int elementTypeCode, int length) {
        this.length = length;
        this.elementTypeCode = elementTypeCode;
    }

    private static int calculateEleType(Node eleNode, Environment env, LineFilePos lineFilePos) {
        SplElement se = eleNode.evaluate(env);
        if (se.valueEquals(env.get("int", lineFilePos))) return SplElement.INT;
        else if (se.valueEquals(env.get("float", lineFilePos))) return SplElement.FLOAT;
        else if (se.valueEquals(env.get("char", lineFilePos))) return SplElement.CHAR;
        else if (se.valueEquals(env.get("boolean", lineFilePos))) return SplElement.BOOLEAN;
        else if (se.valueEquals(env.get("byte", lineFilePos))) return SplElement.BYTE;
        else if (se.valueEquals(env.get("Object", lineFilePos))) return SplElement.POINTER;
        else {
            SplInvokes.throwException(
                    env,
                    Constants.TYPE_ERROR,
                    "Only basic types are valid in array creation.",
                    lineFilePos
            );
            return -1;
        }
//        if (eleNode instanceof NameNode) {
//            String name = ((NameNode) eleNode).getName();
//            switch (name) {
//                case "int":
//                    return SplElement.INT;
//                case "float":
//                    return SplElement.FLOAT;
//                case "char":
//                    return SplElement.CHAR;
//                case "byte":
//                    return SplElement.BYTE;
//                case "boolean":
//                    return SplElement.BOOLEAN;
//                case "Object":  // this case refers to AbstractObject
//                    return SplElement.POINTER;
//                default:
//                    break;
//            }
//        }
//        throw new NativeTypeError("Only basic types are valid in array creation. ");
    }

    public static Reference createArray(int eleType, int arrSize, Environment env) {
        Memory memory = env.getMemory();
        Reference arrPtr = memory.allocate(arrSize + 1, env);
        SplArray arrIns = new SplArray(eleType, arrSize);
        memory.set(arrPtr, arrIns);
        fillInitValue(eleType, arrPtr, memory, arrSize);

        return arrPtr;
    }

    public static SplElement createArray(Node eleNode, int arrSize, Environment env, LineFilePos lineFilePos) {
        int eleType = calculateEleType(eleNode, env, lineFilePos);
        if (env.hasException()) return Undefined.ERROR;
        return createArray(eleType, arrSize, env);
    }

    public static void fillInitValue(int eleType, Reference arrayPtr, Memory memory, int arrayLength) {
        int firstEleAddr = arrayPtr.getPtr() + 1;
        SplElement defaultValue = switch (eleType) {
            case SplElement.INT -> Int.ZERO;
            case SplElement.FLOAT -> SplFloat.ZERO;
            case SplElement.BOOLEAN -> Bool.FALSE;
            case SplElement.CHAR -> Char.NULL_TERMINATOR;
            case SplElement.BYTE -> SplByte.ZERO;
            case SplElement.POINTER -> Reference.NULL;
            default -> throw new NativeTypeError();
        };

        for (int i = 0; i < arrayLength; ++i) {
            memory.set(firstEleAddr + i, defaultValue);
        }
    }

    public static SplElement getItemAtIndex(Reference arrPtr, int index, Environment env, LineFilePos lineFile) {
        SplArray array = (SplArray) env.getMemory().get(arrPtr);
        if (index < 0 || index >= array.length) {
            SplInvokes.throwException(
                    env,
                    Constants.INDEX_ERROR,
                    "Index " + index + " out of array length " + array.length + ".",
                    lineFile);
            return Undefined.ERROR;
        }
        return env.getMemory().getPrimitive(arrPtr.getPtr() + index + 1);
    }

    public static void setItemAtIndex(Reference arrPtr,
                                      int index,
                                      SplElement value,
                                      Environment env,
                                      LineFilePos lineFile) {
        SplArray array = (SplArray) env.getMemory().get(arrPtr);
        if (value.type() == array.elementTypeCode) {
            if (index < 0 || index >= array.length) {
                SplInvokes.throwException(env,
                        Constants.INDEX_ERROR,
                        "Index " + index + " out of array length " + array.length + ". ",
                        lineFile);
                return;
            }
            env.getMemory().set(arrPtr.getPtr() + index + 1, value);
        } else {
            SplInvokes.throwException(env,
                    Constants.TYPE_ERROR,
                    String.format("Array element type: %s, argument type: %s. ",
                            SplElement.typeToString(array.elementTypeCode), SplElement.typeToString(value.type())),
                    lineFile);
        }
    }

    public static char[] toJavaCharArray(Reference arrPtr, Memory memory) {
        int[] lenPtr = toJavaArrayCommon(arrPtr, memory);

        char[] javaCharArray = new char[lenPtr[0]];

        for (int j = 0; j < javaCharArray.length; ++j) {
            SplElement value = memory.getPrimitive(lenPtr[1] + j);
            javaCharArray[j] = (char) value.intValue();
        }

        return javaCharArray;
    }

    public static byte[] toJavaByteArray(Reference arrPtr, Memory memory) {
        int[] lenPtr = toJavaArrayCommon(arrPtr, memory);

        byte[] javaByteArray = new byte[lenPtr[0]];

        for (int i = 0; i < javaByteArray.length; i++) {
            SplElement value = memory.getPrimitive(lenPtr[1] + i);
            javaByteArray[i] = (byte) value.intValue();
        }

        return javaByteArray;
    }

    public static Reference fromJavaArray(byte[] array, Environment env, LineFilePos lineFilePos) {
        Reference arrayRef = createArray(SplElement.BYTE, array.length, env);
        for (int i = 0; i < array.length; i++) {
            setItemAtIndex(arrayRef, i, new SplByte(array[i]), env, lineFilePos);
        }
        return arrayRef;
    }

    private static int[] toJavaArrayCommon(Reference arrPtr, Memory memory) {
        SplArray array = (SplArray) memory.get(arrPtr);

        int firstEleAddr = arrPtr.getPtr() + 1;

        // returns array length and addr of first element
        return new int[]{array.length, firstEleAddr};
    }

    public SplElement getAttr(Node attrNode, Environment env, LineFilePos lineFile) {
        if (attrNode instanceof NameNode) {
            String name = ((NameNode) attrNode).getName();
            if (name.equals(Constants.ARRAY_LENGTH)) {
                return new Int(length);
            } else if (name.equals(Constants.ARRAY_TYPE)) {
                return switch (elementTypeCode) {
                    case SplElement.INT -> env.get("int", lineFile);
                    case SplElement.FLOAT -> env.get("float", lineFile);
                    case SplElement.CHAR -> env.get("char", lineFile);
                    case SplElement.BOOLEAN -> env.get("boolean", lineFile);
                    case SplElement.BYTE -> env.get("byte", lineFile);
                    case SplElement.POINTER -> env.get("Object", lineFile);
                    default -> throw new NativeTypeError();
                };
            }
        }
        return SplInvokes.throwExceptionWithError(
                env,
                Constants.ATTRIBUTE_EXCEPTION,
                "Array does not have attribute '" + attrNode + "'. ",
                lineFile
        );
    }

    @Override
    public String toString() {
        return SplElement.typeToString(elementTypeCode) + "[" + length + "]";
    }
}
