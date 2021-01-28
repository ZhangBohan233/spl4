package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.LambdaExpression;
import spl.interpreter.splObjects.SplCallable;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class LambdaExpressionDef extends Expression {

    private final Line parameters;
    private final Expression body;

    public LambdaExpressionDef(Line parameters, Expression body, LineFilePos lineFile) {
        super(lineFile);

        this.parameters = parameters;
        this.body = body;
    }

    public static LambdaExpressionDef reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Line params = Reconstructor.reconstruct(in);
        Expression body = Reconstructor.reconstruct(in);
        return new LambdaExpressionDef(params, body, lineFilePos);
    }

    @Override
    public String toString() {
        return "lambda(" + parameters + ") -> " + body + ";";
    }

    @Override
    protected SplElement internalEval(Environment env) {
        Function.Parameter[] params = SplCallable.evalParams(parameters, env);
        if (env.hasException()) return Undefined.ERROR;

        LambdaExpression lambdaExpression = new LambdaExpression(body, params, env, getLineFile());

        return env.getMemory().allocateFunction(lambdaExpression, env);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        parameters.save(out);
        body.save(out);
    }
}
