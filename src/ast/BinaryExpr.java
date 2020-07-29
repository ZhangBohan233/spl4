package ast;

import util.LineFile;

public abstract class BinaryExpr extends AbstractExpression implements Buildable {
    protected AbstractExpression left;
    protected AbstractExpression right;
    protected String operator;

    public BinaryExpr(String operator, LineFile lineFile) {
        super(lineFile);
        this.operator = operator;
    }

    @Override
    public String toString() {
        return String.format("BE(%s %s %s)", left, operator, right);
    }

    @Override
    public boolean notFulfilled() {
        return left == null || right == null;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }

    public void setLeft(AbstractExpression left) {
        this.left = left;
    }

    public void setRight(AbstractExpression right) {
        this.right = right;
    }
}
