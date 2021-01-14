package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;

import java.io.IOException;

public class BreakStmt extends Statement {

    public BreakStmt(LineFilePos lineFile) {
        super(lineFile);
    }

    public static BreakStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        return new BreakStmt(lineFilePos);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.breakLoop(lineFile);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
    }
}
