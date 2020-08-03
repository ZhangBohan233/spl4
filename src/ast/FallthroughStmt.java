package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class FallthroughStmt extends AbstractStatement {

    public FallthroughStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.fallthrough(lineFile);
    }
}
