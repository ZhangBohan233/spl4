package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class YieldStmt extends UnaryStmt {

    public YieldStmt(LineFilePos lineFile) {
        super("yield", true, lineFile);
    }

    public static YieldStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        Expression value = Reconstructor.reconstruct(is);
        var rs = new YieldStmt(lineFilePos);
        rs.setValue(value);
        return rs;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        value.save(out);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.yield(value.evaluate(env), lineFile);
    }
}
