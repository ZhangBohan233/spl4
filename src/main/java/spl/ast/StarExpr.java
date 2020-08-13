package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public class StarExpr extends UnaryExpr {

    public StarExpr(LineFile lineFile) {
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
