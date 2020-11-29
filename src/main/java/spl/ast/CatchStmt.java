package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFile;

public class CatchStmt extends Statement {

    final Expression condition;
    final BlockStmt body;

    public CatchStmt(Expression condition, BlockStmt body, LineFile lineFile) {
        super(lineFile);

        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void internalProcess(Environment env) {
        body.evaluate(env);
    }

    @Override
    public String toString() {
        return "catch " + condition + " then " + body;
    }
}
