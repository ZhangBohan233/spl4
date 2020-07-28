package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class CatchStmt extends Node {

    private final AbstractExpression condition;
    private final BlockStmt body;

    public CatchStmt(AbstractExpression condition, BlockStmt body, LineFile lineFile) {
        super(lineFile);

        this.condition = condition;
        this.body = body;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }

    @Override
    public String toString() {
        return "catch " + condition + " then " + body;
    }
}
