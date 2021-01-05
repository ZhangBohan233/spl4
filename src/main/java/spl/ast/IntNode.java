package spl.ast;

import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Int;
import spl.interpreter.env.Environment;
import spl.util.LineFilePos;

public class IntNode extends LiteralNode {
    private final long value;

    public IntNode(long value, LineFilePos lineFile) {
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
}
