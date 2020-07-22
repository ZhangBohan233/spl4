package interpreter.splObjects;

import ast.Arguments;
import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public abstract class NativeFunction extends SplCallable {

    private final String name;
    private final int leastArg;
    private final int mostArg;

    public NativeFunction(String name, int leastArg, int mostArg) {
        this.name = name;
        this.leastArg = leastArg;
        this.mostArg = mostArg;
    }

    public NativeFunction(String name, int argCount) {
        this.name = name;
        leastArg = argCount;
        mostArg = argCount;
    }

    /**
     * Override this method only if the function needs unevaluated argument node.
     * <p>
     * If the function does not need unevaluated argument node, override {@code callFunc} only.
     * If the function needs unevaluated argument node, override both {@code callFuncWithNode} and {@code callFunc}.
     *
     * @param arguments  arguments line, number of arguments is already checked to be validate.
     * @param callingEnv the environment where this call is taking place
     * @return the calling result
     */
    protected SplElement callFuncWithNode(Arguments arguments, Environment callingEnv) {
        SplElement[] evaluatedArgs = arguments.evalArgs(callingEnv);

        return callFunc(evaluatedArgs, callingEnv);
    }

    /**
     * Call this function with arguments already evaluated.
     *
     * @param evaluatedArgs evaluated arguments
     * @param callingEnv    the environment where this call is taking place
     * @return the calling result
     */
    protected abstract SplElement callFunc(SplElement[] evaluatedArgs, Environment callingEnv);

    public SplElement call(Arguments arguments, Environment callingEnv) {
        checkValidArgCount(arguments.getLine().size(), name);

        return callFuncWithNode(arguments, callingEnv);
    }

    public SplElement call(SplElement[] evaluatedArgs, Environment callingEnv, LineFile lineFile) {
        checkValidArgCount(evaluatedArgs.length, name);

        return callFunc(evaluatedArgs, callingEnv);
    }

    @Override
    public String toString() {
        return "NativeFunction{" + name + "}";
    }

    @Override
    public int maxArgCount() {
        return mostArg;
    }

    @Override
    public int minArgCount() {
        return leastArg;
    }
}
