package spl.ast;

import spl.interpreter.splErrors.NativeError;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class AnonymousClassExpr extends BinaryExpr {

    public AnonymousClassExpr(LineFilePos lineFile) {
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
