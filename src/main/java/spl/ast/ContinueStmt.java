package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFile;

public class ContinueStmt extends Statement {

    public ContinueStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.pauseLoop();
    }
}
