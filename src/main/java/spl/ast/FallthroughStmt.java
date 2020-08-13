package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFile;

public class FallthroughStmt extends AbstractStatement {

    public FallthroughStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.fallthrough(lineFile);
    }
}
