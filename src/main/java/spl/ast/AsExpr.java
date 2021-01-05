package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class AsExpr extends BinaryExpr {

    public AsExpr(LineFilePos lineFile) {
        super("as", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }

    @Override
    public NameNode getRight() {
        return (NameNode) right;
    }
}
