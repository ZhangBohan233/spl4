package ast;

import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public abstract class NonEvaluate extends Node {

    public NonEvaluate(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected final SplElement internalEval(Environment env) {
        throw new NativeError("Not evaluate-able. ", getLineFile());
    }
}
