package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public class TypeExpr extends BinaryExpr {

    public TypeExpr(LineFile lineFile) {
        super(":", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }
}
