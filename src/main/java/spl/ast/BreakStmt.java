package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFilePos;

public class BreakStmt extends Statement {

    public BreakStmt(LineFilePos lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.breakLoop(lineFile);
    }
}
