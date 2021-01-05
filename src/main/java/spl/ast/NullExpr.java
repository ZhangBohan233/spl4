package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class NullExpr extends LiteralNode {

    public NullExpr(LineFilePos lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return Reference.NULL_PTR;
    }

    @Override
    public String toString() {
        return "Null";
    }
}
