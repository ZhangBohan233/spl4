package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class BoolLiteral extends LiteralNode {

    private final Bool value;

    public BoolLiteral(boolean val, LineFilePos lineFile) {
        super(lineFile);

        value = Bool.boolValueOf(val);
    }

    public static BoolLiteral reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        boolean value = is.readBoolean();
        return new BoolLiteral(value, lineFilePos);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeBoolean(value.booleanValue());
    }
}
