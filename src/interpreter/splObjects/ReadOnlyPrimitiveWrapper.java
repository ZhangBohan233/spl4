package interpreter.splObjects;

import interpreter.primitives.*;

/**
 * This class is the wrapper class for primitive. Used to store primitive in heap memory (like in array).
 */
public class ReadOnlyPrimitiveWrapper extends SplObject {

    public static ReadOnlyPrimitiveWrapper nullWrapper() {
        return new ReadOnlyPrimitiveWrapper(Pointer.NULL_PTR);
    }

    public static ReadOnlyPrimitiveWrapper intZeroWrapper() {
        return new ReadOnlyPrimitiveWrapper(new Int(0));
    }

    public static ReadOnlyPrimitiveWrapper charZeroWrapper() {
        return new ReadOnlyPrimitiveWrapper(new Char('\0'));
    }

    public static ReadOnlyPrimitiveWrapper floatZeroWrapper() {
        return new ReadOnlyPrimitiveWrapper(new SplFloat(0));
    }

    public static ReadOnlyPrimitiveWrapper booleanFalseWrapper() {
        return new ReadOnlyPrimitiveWrapper(new Bool(false));
    }

    public final SplElement value;

    public ReadOnlyPrimitiveWrapper(SplElement value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "W{" + value + "}";
    }
}
