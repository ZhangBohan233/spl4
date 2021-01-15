package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.io.OutputStream;

public class ContinueStmt extends Statement {

    public ContinueStmt(LineFilePos lineFile) {
        super(lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.pauseLoop(lineFile);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
    }

    public static ContinueStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        return new ContinueStmt(lineFilePos);
    }
}
