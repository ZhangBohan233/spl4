package spl.ast;

import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Int;
import spl.interpreter.env.Environment;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IntLiteral extends LiteralNode {
    private final long value;

    public IntLiteral(long value, LineFilePos lineFile) {
        super(lineFile);

        this.value = value;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return new Int(value);
    }

    @Override
    public String toString() {
        return "Int(" + value + ')';
    }

    public long getValue() {
        return value;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeLong(value);
    }

    public static IntLiteral reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        return new IntLiteral(is.readLong(), lineFilePos);
    }
}
