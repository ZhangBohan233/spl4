package ast;

import util.LineFile;

public abstract class UnaryExpr extends Expr {

    protected final String operator;
    protected Node value;
    public final boolean atLeft;

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

    public String getOperator() {
        return operator;
    }

    public void setValue(Node value) {
        this.value = value;
    }

    public Node getValue() {
        return value;
    }
}
