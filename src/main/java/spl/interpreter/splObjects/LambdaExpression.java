package spl.interpreter.splObjects;

import spl.ast.AbstractExpression;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.FunctionEnvironment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public class LambdaExpression extends UserFunction {

    private static int count = 0;

    private final int lambdaId = count++;

    private final AbstractExpression body;

    public LambdaExpression(AbstractExpression body, SplCallable.Parameter[] params, Environment definitionEnv,
                            LineFile lineFile) {

        super(params, definitionEnv, lineFile);

        this.body = body;
    }

    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile lineFile) {
        String name = toString();
        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, name);

        checkValidArgCount(evaluatedArgs.positionalArgs.size(), name);

        setArgs(evaluatedArgs, scope);

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
