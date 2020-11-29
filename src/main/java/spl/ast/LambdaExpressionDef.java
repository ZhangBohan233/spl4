package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.LambdaExpression;
import spl.interpreter.splObjects.SplCallable;
import spl.util.LineFile;

public class LambdaExpressionDef extends Expression {

    private final Line parameters;
    private final Expression body;

    public LambdaExpressionDef(Line parameters, Expression body, LineFile lineFile) {
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
