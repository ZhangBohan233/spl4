package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class AnonymousClassExpr extends BinaryExpr {

    public AnonymousClassExpr(LineFile lineFile) {
        super("<-", lineFile);
    }

    Node getContent() {
        return right;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        throw new SplException("Not evaluate-able. ", getLineFile());
    }
}
