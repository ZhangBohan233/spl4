package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class InExpr extends BinaryExpr {

    public InExpr(LineFilePos lineFile) {
        super("in", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }
}
