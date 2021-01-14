package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;

import java.io.IOException;

public class FallthroughStmt extends Statement {

    public FallthroughStmt(LineFilePos lineFile) {
        super(lineFile);
    }

    public static FallthroughStmt reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        return new FallthroughStmt(lineFilePos);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.fallthrough(lineFile);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
    }
}
