package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.io.OutputStream;

public class NullExpr extends LiteralNode {

    public NullExpr(LineFilePos lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return Reference.NULL;
    }

    public static NullExpr reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        return new NullExpr(lineFilePos);
    }

    @Override
    public String toString() {
        return "Null";
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {

    }
}
