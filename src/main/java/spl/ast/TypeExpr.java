package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

public class TypeExpr extends BinaryExpr {

    public TypeExpr(LineFilePos lineFile) {
        super(":", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }

    public static TypeExpr reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        in.readString();  // op
        Expression left = Reconstructor.reconstruct(in);
        Expression right = Reconstructor.reconstruct(in);
        var be = new TypeExpr(lineFilePos);
        be.setLeft(left);
        be.setRight(right);
        return be;
    }
}
