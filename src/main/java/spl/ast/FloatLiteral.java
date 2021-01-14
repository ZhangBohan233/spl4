package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.SplFloat;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.io.OutputStream;

public class FloatLiteral extends LiteralNode {

    public final double value;

    public FloatLiteral(double value, LineFilePos lineFile) {
        super(lineFile);

        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    protected SplElement internalEval(Environment env) {

        return new SplFloat(value);
    }

    @Override
    public String toString() {
        return "Float(" + value + ")";
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeDouble(value);
    }

    public static FloatLiteral reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        return new FloatLiteral(in.readDouble(), lineFilePos);
    }
}
