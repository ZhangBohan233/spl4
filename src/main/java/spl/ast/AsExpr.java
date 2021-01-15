package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.io.OutputStream;

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

    public static AsExpr reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String op = is.readString();
        Expression left = Reconstructor.reconstruct(is);
        Expression right = Reconstructor.reconstruct(is);
        AsExpr be = new AsExpr(lineFilePos);
        be.setLeft(left);
        be.setRight(right);
        return be;
    }
}
