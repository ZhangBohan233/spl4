package spl.ast;

import spl.util.BytesOut;
import spl.util.LineFilePos;

import java.io.IOException;

public abstract class BinaryExpr extends Expression implements Buildable {
    protected final String operator;
    transient protected final boolean isLeftFirst;  // whether to evaluate left side at first
    protected Expression left;
    protected Expression right;

    public BinaryExpr(String operator, boolean isLeftFirst, LineFilePos lineFile) {
        super(lineFile);
        this.operator = operator;
        this.isLeftFirst = isLeftFirst;
    }

    public BinaryExpr(String operator, LineFilePos lineFilePos) {
        this(operator, true, lineFilePos);
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

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isLeftFirst() {
        return isLeftFirst;
    }

    @Override
    public String reprString() {
        return operator;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(operator);
        left.save(out);
        right.save(out);
    }
}
