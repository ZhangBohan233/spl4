package spl.interpreter.primitives;

import spl.interpreter.splErrors.TypeError;

public class Pointer extends SplElement {

    private final int ptr;

    public static final Pointer NULL_PTR = new Pointer(0);

    public Pointer(int ptr) {
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
        throw new TypeError("Cannot convert pointer to float. ");
    }

    @Override
    public String toString() {
        return ptr == 0 ? "null" : "Ptr<" + ptr + ">";
    }

    public int getPtr() {
        return ptr;
    }

    @Override
    public boolean isIntLike() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pointer pointer = (Pointer) o;

        return ptr == pointer.ptr;
    }

    @Override
    public int hashCode() {
        return ptr;
    }
}
