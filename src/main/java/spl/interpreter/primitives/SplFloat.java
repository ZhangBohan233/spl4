package spl.interpreter.primitives;

public class SplFloat extends SplElement {

    public static final SplFloat ZERO = new SplFloat(0.0d);

    public final double value;

    public SplFloat(double value) {
        this.value = value;
    }

    @Override
    public boolean isIntLike() {
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int type() {
        return SplElement.FLOAT;
    }

    @Override
    public long intValue() {
        return (long) value;
    }

    @Override
    public double floatValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SplFloat anInt = (SplFloat) o;

        return value == anInt.value;
    }
}
