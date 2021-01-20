package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplByte;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;

import java.io.IOException;

public class ByteLiteral extends LiteralNode {

    public final byte b;

    public ByteLiteral(byte b, LineFilePos lineFile) {
        super(lineFile);

        this.b = b;
    }

    public static ByteLiteral reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        return new ByteLiteral(in.readByte(), lineFilePos);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return new SplByte(b);
    }

    @Override
    public String toString() {
        return "Byte(" + b + ")";
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.write(b);
    }

    public byte getValue() {
        return b;
    }
}

