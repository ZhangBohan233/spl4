package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public abstract class AbstractStatement extends Node {

    public AbstractStatement(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
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
