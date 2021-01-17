package spl.interpreter.splObjects;

import spl.ast.Expression;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.util.LineFilePos;

/**
 * Anonymous function
 * <p>
 * Note that lambda expression does not support templates and docstrings.
 */
public class LambdaExpression extends UserFunction {

    private static int count = 0;

    private final int lambdaId = count++;

    private final Expression body;

    public LambdaExpression(Expression body, SplCallable.Parameter[] params, Environment definitionEnv,
                            LineFilePos lineFile) {

        super(params, definitionEnv, lineFile);

        this.body = body;
    }

    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Reference[] generics,
                           Environment callingEnv, LineFilePos lineFile) {
        String name = toString();
        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, name);

        checkValidArgCount(evaluatedArgs.positionalArgs.size() + evaluatedArgs.keywordArgs.size(),
                name, callingEnv, lineFile);
        if (callingEnv.hasException()) return Undefined.ERROR;

        setArgs(evaluatedArgs, scope, callingEnv, lineFile);

        scope.getMemory().pushStack(scope, lineFile);
        SplElement evalResult = body.evaluate(scope);
        scope.getMemory().decreaseStack();

        return evalResult;
    }

    @Override
    public String toString() {
        return "lambda#" + lambdaId;
    }
}
