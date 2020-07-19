package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import interpreter.primitives.SplFloat;
import util.LineFile;

public class FloatNode extends LiteralNode {

    public final double value;

    public FloatNode(double value, LineFile lineFile) {
        super(lineFile);

        this.value = value;
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
