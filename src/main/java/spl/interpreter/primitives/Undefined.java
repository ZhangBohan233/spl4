package spl.interpreter.primitives;

public class Undefined extends SplElement {

    public static final Undefined UNDEFINED = new Undefined("undefined");
    public static final Undefined ERROR = new Undefined("error");

    private final String text;

    private Undefined(String text) {
        this.text = text;
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
    public boolean booleanValue() {
        return false;
    }

    @Override
    public boolean isIntLike() {
        return false;
    }

    @Override
    public String toString() {
        return text;
    }
}
