package spl.ast;

import spl.util.LineFile;

public abstract class UnaryExpr extends AbstractExpression implements UnaryBuildable {

    protected final String operator;
    protected AbstractExpression value;
    private final boolean atLeft;

    public UnaryExpr(String operator, boolean operatorAtLeft, LineFile lineFile) {
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

    @Override
    public void setValue(Node value) {
        this.value = (AbstractExpression) value;
    }

    public Node getValue() {
        return value;
    }
}
