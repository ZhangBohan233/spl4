package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.SplFloat;
import spl.util.LineFilePos;

public class FloatNode extends LiteralNode {

    public final double value;

    public FloatNode(double value, LineFilePos lineFile) {
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
        return String.valueOf(value);
    }
}
