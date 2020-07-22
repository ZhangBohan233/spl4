package interpreter.splObjects;

import ast.AbstractExpression;
import ast.BlockStmt;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.primitives.SplElement;
import util.LineFile;

import java.util.Arrays;

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
    public SplElement call(SplElement[] evaluatedArgs, Environment callingEnv, LineFile lineFile) {
        String name = toString();
        FunctionEnvironment scope = new FunctionEnvironment(definitionEnv, callingEnv, name);

        checkValidArgCount(evaluatedArgs.length, name);

        putArgsToScope(evaluatedArgs, scope);

        scope.getMemory().pushStack(scope);
        SplElement evalResult = body.evaluate(scope);
        scope.getMemory().decreaseStack();

        return evalResult;
    }

    @Override
    public String toString() {
        return "lambda#" + lambdaId;
    }
}
