package spl.interpreter.primitives;

import spl.interpreter.splErrors.NativeTypeError;

public class Reference extends SplElement {

    private int ptr;

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
        return ptr == 0 ? "Ref<null>" : "Ref<" + ptr + ">";
    }

    public int getPtr() {
        return ptr;
    }

    /**
     * This method should only be used by garbage collection system.
     *
     * @param ptr the new pointer
     */
    public void setPtr(int ptr) {
        this.ptr = ptr;
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
        return this == o;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean valueEquals(SplElement other) {
        return getClass() == other.getClass() && ptr == ((Reference) other).ptr;
    }
}
