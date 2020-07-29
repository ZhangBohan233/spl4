package ast;

import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Function;
import interpreter.splObjects.LambdaExpression;
import interpreter.splObjects.SplCallable;
import util.LineFile;

public class LambdaExpressionDef extends AbstractExpression {

    private final Line parameters;
    private final AbstractExpression body;

    public LambdaExpressionDef(Line parameters, AbstractExpression body, LineFile lineFile) {
        super(lineFile);

        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public String toString() {
        return "lambda(" + parameters + ") -> " + body + ";";
    }

    @Override
    protected SplElement internalEval(Environment env) {
        Function.Parameter[] params = SplCallable.evalParams(parameters, env);
        LambdaExpression lambdaExpression = new LambdaExpression(body, params, env, getLineFile());

        return env.getMemory().allocateFunction(lambdaExpression, env);

    }
}
