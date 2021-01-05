package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFilePos;

public class ContinueStmt extends Statement {

    public ContinueStmt(LineFilePos lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.pauseLoop(lineFile);
    }
}
