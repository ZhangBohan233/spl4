package interpreter.splObjects;

import ast.NameNode;
import ast.Node;
import interpreter.AttributeError;
import interpreter.Memory;
import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.*;
import interpreter.types.*;
import util.LineFile;

import java.util.List;

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

    public SplElement getAttr(Node attrNode, LineFile lineFile) {
        if (attrNode instanceof NameNode && ((NameNode) attrNode).getName().equals("length")) {
            return new Int(length);
        } else {
            throw new AttributeError("Array does not have attribute '" + attrNode + "'. ", lineFile);
        }
    }

    @Override
    public String toString() {
        return SplElement.typeToString(elementTypeCode) + "[" + length + "]";
    }

//    public static Pointer createArray(List<Integer> dimensions, Environment env) {
//        return createArray(dimensions, env, 0);
//    }

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
        throw new TypeError("Only basic types are valid in array creation.");
    }

    public static Pointer createArray(int eleType, int arrSize, Environment env) {
        Memory memory = env.getMemory();
        Pointer arrPtr = memory.allocate(arrSize + 1, env);
        SplArray arrIns = new SplArray(eleType, arrSize);
        memory.set(arrPtr, arrIns);
        fillInitValue(eleType, arrPtr, memory, arrSize);

        return arrPtr;
    }

    public static Pointer createArray(Node eleNode, int arrSize, Environment env) {
        return createArray(calculateEleType(eleNode), arrSize, env);
//        int arrSize = dimensions.get(proceedingIndex);
//        if (arrSize == -1) {
//            return Pointer.NULL_PTR;
//        } else {
//            int eleType = calculateEleType(eleNode);
//            Memory memory = env.getMemory();
//            Pointer arrPtr = memory.allocate(arrSize + 1, env);
//            SplArray arrIns = new SplArray(eleType, arrSize);
//            memory.set(arrPtr, arrIns);
////            if (proceedingIndex == dimensions.size() - 1) {
//            fillInitValue(eleType, arrPtr, memory, arrSize);
////            } else{
////                int firstEleAddr = arrPtr.getPtr() + 1;
////                for (int i = 0; i < arrSize; ++i) {
////                    Pointer innerArrPtr = createArray(((ArrayType) eleType).getEleType(),
////                            dimensions,
////                            env,
////                            proceedingIndex + 1);
////                    memory.set(firstEleAddr + i, new ReadOnlyPrimitiveWrapper(innerArrPtr));
////                }
////            }
//            return arrPtr;
//        }
    }

    public static void fillInitValue(int eleType, Pointer arrayPtr, Memory memory, int arrayLength) {
        int firstEleAddr = arrayPtr.getPtr() + 1;
        SplElement defaultValue;

        switch (eleType) {
            case SplElement.INT:
                defaultValue = Int.ZERO;
                break;
            case SplElement.FLOAT:
                defaultValue = SplFloat.ZERO;
                break;
            case SplElement.BOOLEAN:
                defaultValue = Bool.FALSE;
                break;
            case SplElement.CHAR:
                defaultValue = Char.NULL_TERMINATOR;
                break;
            case SplElement.POINTER:
                defaultValue = Pointer.NULL_PTR;
                break;
            default:
                throw new TypeError();
        }

        for (int i = 0; i < arrayLength; ++i) {
            memory.set(firstEleAddr + i, defaultValue);
        }
    }

    public static SplElement getItemAtIndex(Pointer arrPtr, int index, Memory memory, LineFile lineFile) {
        SplArray array = (SplArray) memory.get(arrPtr);
        if (index < 0 || index >= array.length) {
            throw new SplException("Index " + index + " out of array length " + array.length + ". ", lineFile);
        }
        return memory.getPrimitive(arrPtr.getPtr() + index + 1);
    }

    public static void setItemAtIndex(Pointer arrPtr,
                                      int index,
                                      SplElement value,
                                      Environment env,
                                      LineFile lineFile) {
        SplArray array = (SplArray) env.getMemory().get(arrPtr);
        if (value.type() == array.elementTypeCode) {
            if (index < 0 || index >= array.length) {
                throw new SplException("Index " + index + " out of array length " + array.length + ". ", lineFile);
            }
            env.getMemory().set(arrPtr.getPtr() + index + 1, value);
        } else {
            throw new TypeError(String.format("Array element type: %s, argument type: %s. ",
                    SplElement.typeToString(array.elementTypeCode), SplElement.typeToString(value.type())));
        }
    }

    public static char[] toJavaCharArray(Pointer arrPtr, Memory memory) {
        int[] lenPtr = toJavaArrayCommon(arrPtr, memory);

        char[] javaCharArray = new char[lenPtr[0]];

        for (int j = 0; j < javaCharArray.length; ++j) {
            SplElement value = memory.getPrimitive(lenPtr[1] + j);
            javaCharArray[j] = (char) value.intValue();
        }

        return javaCharArray;
    }

    private static int[] toJavaArrayCommon(Pointer arrPtr, Memory memory) {
        SplArray array = (SplArray) memory.get(arrPtr);

        int firstEleAddr = arrPtr.getPtr() + 1;

        // returns array length and addr of first element
        return new int[]{array.length, firstEleAddr};
    }
}
