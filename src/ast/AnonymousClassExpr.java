package ast;

import interpreter.splErrors.NativeError;
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
        throw new NativeError("Not evaluate-able. ", getLineFile());
    }
}
