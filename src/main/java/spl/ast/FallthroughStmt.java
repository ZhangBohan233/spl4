package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFilePos;

public class FallthroughStmt extends Statement {

    public FallthroughStmt(LineFilePos lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.fallthrough(lineFile);
    }
}
