package spl.ast;

import spl.util.LineFilePos;

public abstract class UnaryStmt extends Statement implements UnaryBuildable {

    protected final String operator;
    private final boolean atLeft;
    protected Expression value;

    public UnaryStmt(String operator, boolean operatorAtLeft, LineFilePos lineFile) {
        super(lineFile);

        this.operator = operator;
        this.atLeft = operatorAtLeft;
    }

    /**
     * @return {@code true} if and only if this {@code UnaryStmt} can take no value.
     */
    public boolean voidAble() {
        return false;
    }

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public boolean operatorAtLeft() {
        return atLeft;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (atLeft) {
            return String.format("US(%s %s)", operator, value);
        } else {
            return String.format("US(%s %s)", value, operator);
        }
    }

    @Override
    public boolean notFulfilled() {
        return value == null;
    }

    @Override
    public String reprString() {
        return operator;
    }
}
