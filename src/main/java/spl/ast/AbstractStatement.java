package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public abstract class AbstractStatement extends Node {

    public AbstractStatement(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected final SplElement internalEval(Environment env) {
        internalProcess(env);
        return null;
    }

    /**
     * The core evaluation method, no returning value
     *
     * @param env the environment
     */
    protected abstract void internalProcess(Environment env);
}
