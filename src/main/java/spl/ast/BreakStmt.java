package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFile;

public class BreakStmt extends AbstractStatement {

    public BreakStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.breakLoop();
    }

}
