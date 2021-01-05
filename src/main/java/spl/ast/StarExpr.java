package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class StarExpr extends UnaryExpr {

    public StarExpr(LineFilePos lineFile) {
        super("star", true, lineFile);
    }

    @Override
    public String toString() {
        return String.format("(*%s)", value);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }
}
