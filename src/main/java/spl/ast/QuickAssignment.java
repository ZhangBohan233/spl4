package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.lexer.SyntaxError;
import spl.util.LineFile;

public class QuickAssignment extends BinaryExpr {

    public QuickAssignment(LineFile lineFile) {
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
}
