package spl.ast;

import spl.util.LineFilePos;

public abstract class UnaryExpr extends Expression implements UnaryBuildable {

    protected final String operator;
    private final boolean atLeft;
    protected Expression value;

    public UnaryExpr(String operator, boolean operatorAtLeft, LineFilePos lineFile) {
        super(lineFile);

        this.operator = operator;
        this.atLeft = operatorAtLeft;
    }

    /**
     * @return {@code true} if and only if this {@code UnaryExpr} can take no value.
     */
    public boolean voidAble() {
        return false;
    }

    @Override
    public boolean notFulfilled() {
        return value == null;
    }

    @Override
    public String toString() {
        if (atLeft) {
            return String.format("UE(%s %s)", operator, value);
        } else {
            return String.format("UE(%s %s)", value, operator);
        }
    }

    @Override
    public boolean operatorAtLeft() {
        return atLeft;
    }

    @Override
    public String getOperator() {
        return operator;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public String reprString() {
        return operator;
    }
}
