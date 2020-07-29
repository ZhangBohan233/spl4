package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class InExpr extends BinaryExpr {

    public InExpr(LineFile lineFile) {
        super("in", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }
}
