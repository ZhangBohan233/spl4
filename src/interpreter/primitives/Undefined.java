package interpreter.primitives;

public class Undefined extends SplElement {

    public static final Undefined UNDEFINED = new Undefined();

    private Undefined() {
    }

    @Override
    public int type() {
        return SplElement.UNDEFINED;
    }

    @Override
    public long intValue() {
        return 0;
    }

    @Override
    public double floatValue() {
        return 0;
    }

    @Override
    public boolean isIntLike() {
        return false;
    }

    @Override
    public String toString() {
        return "undefined";
    }
}
