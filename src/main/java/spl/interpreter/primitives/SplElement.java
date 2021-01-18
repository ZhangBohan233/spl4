package spl.interpreter.primitives;

import spl.interpreter.SplThing;
import spl.interpreter.splErrors.NativeTypeError;

public abstract class SplElement implements SplThing {

    public static final int VOID = 0;
    public static final int INT = 1;
    public static final int FLOAT = 2;
    public static final int CHAR = 3;
    public static final int BOOLEAN = 4;
    public static final int POINTER = 5;
    public static final int UNDEFINED = 6;
    public static final int BYTE = 7;

    public abstract int type();

    public abstract long intValue();

    public abstract boolean booleanValue();

    public abstract double floatValue();

    public abstract boolean isIntLike();

    public static String typeToString(int type) {
        return switch (type) {
            case INT -> "int";
            case FLOAT -> "float";
            case CHAR -> "char";
            case BOOLEAN -> "boolean";
            case VOID -> "void";
            case POINTER -> "Obj";
            case UNDEFINED -> "undefinedType";
            case BYTE -> "byte";
            default -> throw new NativeTypeError();
        };
    }

    public static boolean isPrimitive(SplElement element) {
        return !(element instanceof Reference);
    }
}
