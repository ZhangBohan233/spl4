package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class StringLiteralRef extends LiteralNode {

    private final StringLiteral literal;

    public StringLiteralRef(StringLiteral literal, LineFilePos lineFile) {
        super(lineFile);

        this.literal = literal;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return literal.evalRef(env, lineFile);
    }

    @Override
    public String toString() {
        return "Ref" + literal.toString();
    }
}
