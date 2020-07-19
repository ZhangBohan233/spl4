package ast;

import interpreter.primitives.SplElement;
import interpreter.primitives.Int;
import interpreter.env.Environment;
import util.LineFile;

public class IntNode extends LiteralNode {
    private final long value;

    public IntNode(long value, LineFile lineFile) {
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
}
