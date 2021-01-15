package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class StarExpr extends UnaryExpr {

    public StarExpr(LineFilePos lineFile) {
        super("star", true, lineFile);
    }

    @Override
    public String toString() {
        return String.format("(*%s)", value);
    }

    public static StarExpr reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Expression value = Reconstructor.reconstruct(in);
        var se = new StarExpr(lineFilePos);
        se.setValue(value);
        return se;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        value.save(out);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }
}
