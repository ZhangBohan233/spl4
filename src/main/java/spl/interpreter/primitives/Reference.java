package spl.interpreter.primitives;

import spl.interpreter.splErrors.NativeTypeError;

public class Reference extends SplElement {

    private final int ptr;

    public static final Reference NULL = new Reference(0);

    public Reference(int ptr) {
        this.ptr = ptr;
    }

    @Override
    public int type() {
        return SplElement.POINTER;
    }

    @Override
    public long intValue() {
        return ptr;
    }

    @Override
    public double floatValue() {
        throw new NativeTypeError("Cannot convert pointer to float. ");
    }

    @Override
    public String toString() {
        return ptr == 0 ? "null" : "Ref<" + ptr + ">";
    }

    public int getPtr() {
        return ptr;
    }

    @Override
    public boolean booleanValue() {
        throw new NativeTypeError("Cannot convert pointer to boolean. ");
    }

    @Override
    public boolean isIntLike() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference pointer = (Reference) o;

        return ptr == pointer.ptr;
    }

    @Override
    public int hashCode() {
        return ptr;
    }
}
