package spl.ast;

import spl.interpreter.splErrors.NativeError;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public abstract class NonEvaluate extends Node {

    public NonEvaluate(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected final SplElement internalEval(Environment env) {
        throw new NativeError("Not evaluate-able. ", getLineFile());
    }
}
