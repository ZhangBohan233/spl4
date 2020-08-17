package spl.interpreter.primitives;

import spl.interpreter.SplThing;
import spl.interpreter.splErrors.TypeError;

public abstract class SplElement implements SplThing {

    public static final int VOID = 0;
    public static final int INT = 1;
    public static final int FLOAT = 2;
    public static final int CHAR = 3;
    public static final int BOOLEAN = 4;
    public static final int POINTER = 5;
    public static final int UNDEFINED = 6;

    public abstract int type();

    public abstract long intValue();

    public abstract boolean booleanValue();

    public abstract double floatValue();

    public abstract boolean isIntLike();

    public char charValue() {
        return (char) intValue();
    }

    public static String typeToString(int type) {
        return switch (type) {
            case INT -> "int";
            case FLOAT -> "float";
            case CHAR -> "char";
            case BOOLEAN -> "boolean";
            case VOID -> "void";
            case POINTER -> "Object";
            case UNDEFINED -> "undefined";
            default -> throw new TypeError();
        };
    }

    public static boolean isPrimitive(SplElement element) {
        return !(element instanceof Pointer);
    }
}