package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.lexer.SyntaxError;
import spl.util.BytesIn;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

public class QuickAssignment extends BinaryExpr {

    public QuickAssignment(LineFilePos lineFile) {
        super(":=", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement rtv = right.evaluate(env);
        if (left instanceof NameNode) {
            env.defineVarAndSet(((NameNode) left).getName(), rtv, getLineFile());
            return rtv;
        } else {
            throw new SyntaxError("Left side of ':=' must be a local name. ", getLineFile());
        }
    }

    public static QuickAssignment reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        in.readString();  // op
        Expression left = Reconstructor.reconstruct(in);
        Expression right = Reconstructor.reconstruct(in);
        var be = new QuickAssignment(lineFilePos);
        be.setLeft(left);
        be.setRight(right);
        return be;
    }
}
