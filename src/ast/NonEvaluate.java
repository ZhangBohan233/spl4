package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public abstract class NonEvaluate extends Node {

    public NonEvaluate(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        throw new SplException("Not evaluate-able. ", getLineFile());
    }
}
