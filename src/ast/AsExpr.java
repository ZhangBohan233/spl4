package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class AsExpr extends BinaryExpr {

    public AsExpr(LineFile lineFile) {
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
