package spl.interpreter.primitives;

public class SplByte extends SplElement {
    public static final SplByte ZERO = new SplByte((byte) 0);

    public final byte value;

    public SplByte(byte value) {
        this.value = value;
    }

    @Override
    public int type() {
        return SplElement.BYTE;
    }

    @Override
    public long intValue() {
        return value & 0xff;
    }

    @Override
    public boolean booleanValue() {
        return value != 0;
    }

    @Override
    public double floatValue() {
        return value;
    }

    @Override
    public boolean isIntLike() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SplByte splByte = (SplByte) o;

        return value == splByte.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value & 0xff);
    }
}
