package spl.interpreter.primitives;

public class Int extends SplElement {

    public static final Int ZERO = new Int(0);
    public static final Int NEG_ONE = new Int(-1);

    public final long value;

    public Int(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean isIntLike() {
        return true;
    }

    @Override
    public int type() {
        return SplElement.INT;
    }

    @Override
    public long intValue() {
        return value;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Int anInt = (Int) o;

        return value == anInt.value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }
}
