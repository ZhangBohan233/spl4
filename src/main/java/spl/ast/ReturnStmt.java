package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.*;

import java.io.IOException;
import java.io.InputStream;

public class ReturnStmt extends UnaryStmt {

    public ReturnStmt(LineFilePos lineFile) {
        super("return", true, lineFile);
    }

    @Override
    public boolean voidAble() {
        return true;
    }

    @Override
    protected void internalProcess(Environment env) {
        env.setReturn(value.evaluate(env), lineFile);
    }

    public static ReturnStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        Expression value = Reconstructor.reconstruct(is);
        ReturnStmt rs = new ReturnStmt(lineFilePos);
        rs.setValue(value);
        return rs;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        value.save(out);
    }
}
