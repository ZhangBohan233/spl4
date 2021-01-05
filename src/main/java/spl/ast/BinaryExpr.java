package spl.ast;

import spl.util.LineFilePos;

public abstract class BinaryExpr extends Expression implements Buildable {
    protected Expression left;
    protected Expression right;
    protected String operator;

    public BinaryExpr(String operator, LineFilePos lineFile) {
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

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    @Override
    public String reprString() {
        return operator;
    }
}
