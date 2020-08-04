package ast;

import interpreter.env.Environment;
import util.LineFile;

public abstract class UnaryStmt extends AbstractStatement implements UnaryBuildable {

    protected final String operator;
    protected Node value;
    private final boolean atLeft;

    public UnaryStmt(String operator, boolean operatorAtLeft, LineFile lineFile) {
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

    @Override
    public void setValue(Node value) {
        this.value = value;
    }

    public Node getValue() {
        return value;
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
}
