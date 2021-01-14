package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

public class InExpr extends BinaryExpr {

    public InExpr(LineFilePos lineFile) {
        super("in", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }

    public static InExpr reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        in.readString();  // op
        Expression left = Reconstructor.reconstruct(in);
        Expression right = Reconstructor.reconstruct(in);
        var be = new InExpr(lineFilePos);
        be.setLeft(left);
        be.setRight(right);
        return be;
    }
}
