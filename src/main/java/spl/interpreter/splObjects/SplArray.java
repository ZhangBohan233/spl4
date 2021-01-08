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

    private static int calculateEleType(Node eleNode) {
        if (eleNode instanceof NameNode) {
            String name = ((NameNode) eleNode).getName();
            switch (name) {
                case "int":
                    return SplElement.INT;
                case "float":
                    return SplElement.FLOAT;
                case "char":
                    return SplElement.CHAR;
                case "boolean":
                    return SplElement.BOOLEAN;
                case "Object":
                    return SplElement.POINTER;
                default:
                    break;
            }
        }
        throw new NativeTypeError("Only basic types are valid in array creation.");
    }

    public static Reference createArray(int eleType, int arrSize, Environment env) {
        Memory memory = env.getMemory();
        Reference arrPtr = memory.allocate(arrSize + 1, env);
        SplArray arrIns = new SplArray(eleType, arrSize);
        memory.set(arrPtr, arrIns);
        fillInitValue(eleType, arrPtr, memory, arrSize);

        return arrPtr;
    }

    public static Reference createArray(Node eleNode, int arrSize, Environment env) {
        return createArray(calculateEleType(eleNode), arrSize, env);
    }

    public static void fillInitValue(int eleType, Reference arrayPtr, Memory memory, int arrayLength) {
        int firstEleAddr = arrayPtr.getPtr() + 1;
        SplElement defaultValue = switch (eleType) {
            case SplElement.INT -> Int.ZERO;
            case SplElement.FLOAT -> SplFloat.ZERO;
            case SplElement.BOOLEAN -> Bool.FALSE;
            case SplElement.CHAR -> Char.NULL_TERMINATOR;
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
//            throw new ArrayIndexError("Index " + index + " out of array length " + array.length + ". ", lineFile);
            SplInvokes.throwException(env, Constants.INDEX_ERROR, "", lineFile);
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

    private static int[] toJavaArrayCommon(Reference arrPtr, Memory memory) {
        SplArray array = (SplArray) memory.get(arrPtr);

        int firstEleAddr = arrPtr.getPtr() + 1;

        // returns array length and addr of first element
        return new int[]{array.length, firstEleAddr};
    }

    public SplElement getAttr(Node attrNode, Environment env, LineFilePos lineFile) {
        if (attrNode instanceof NameNode && ((NameNode) attrNode).getName().equals(Constants.ARRAY_LENGTH)) {
            return new Int(length);
        } else {
            SplInvokes.throwException(
                    env,
                    Constants.ATTRIBUTE_EXCEPTION,
                    "Array does not have attribute '" + attrNode + "'. ",
                    lineFile
            );
            return Undefined.ERROR;
        }
    }

    @Override
    public String toString() {
        return SplElement.typeToString(elementTypeCode) + "[" + length + "]";
    }

    @Override
    protected int gcGenerationLimit() {
        return 2;
    }
}
