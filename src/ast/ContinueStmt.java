package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class ContinueStmt extends AbstractStatement {

    public ContinueStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.pauseLoop();
    }
}
