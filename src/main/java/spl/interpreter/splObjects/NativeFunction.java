package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.util.LineFilePos;

/**
 * Native function, implemented in Java.
 * <p>
 * Note that native functions support neither variable length arguments nor keyword arguments.
 */
public abstract class NativeFunction extends SplCallable {

    private final String name;
    private final int argCount;

    public NativeFunction(String name, int argCount) {
        this.name = name;
        this.argCount = argCount;
    }

    @Override
    public String getName() {
        return name;
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
        EvaluatedArguments evaluatedArgs = arguments.evalArgs(callingEnv);
        if (callingEnv.hasException()) return Undefined.ERROR;

        return callFunc(evaluatedArgs, callingEnv, arguments.getLineFile());
    }

    /**
     * Call this function with arguments already evaluated.
     *
     * @param evaluatedArgs evaluated arguments
     * @param callingEnv    the environment where this call is taking place
     * @param callingLfp    the position of calling
     * @return the calling result
     */
    protected abstract SplElement callFunc(EvaluatedArguments evaluatedArgs,
                                           Environment callingEnv,
                                           LineFilePos callingLfp);

    public SplElement call(Arguments arguments, Environment callingEnv) {
        checkValidArgCount(arguments.getLine().size(), 0, name, callingEnv, arguments.getLineFile());
        if (callingEnv.hasException()) return Undefined.ERROR;

        return callFuncWithNode(arguments, callingEnv);
    }

    public SplElement call(EvaluatedArguments evaluatedArgs, Reference[] generics,
                           Environment callingEnv, LineFilePos lineFile) {
        checkValidArgCount(evaluatedArgs.positionalArgs.size(), evaluatedArgs.keywordArgs.size(),
                name, callingEnv, lineFile);
        if (callingEnv.hasException()) return Undefined.ERROR;

        return callFunc(evaluatedArgs, callingEnv, lineFile);
    }

    @Override
    public String toString() {
        return "NativeFunction{" + name + "}";
    }

    @Override
    public int maxPosArgCount() {
        return argCount;
    }

    @Override
    public int minPosArgCount() {
        return argCount;
    }

    @Override
    public int maxKwArgCount() {
        return 0;
    }
}
