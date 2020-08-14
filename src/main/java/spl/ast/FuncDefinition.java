package spl.ast;

import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Function;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplCallable;
import spl.util.LineFile;

public class FuncDefinition extends AbstractExpression {

    public final String name;
    private Line parameters;
    private BlockStmt body;

    public FuncDefinition(String name, Line parameters, BlockStmt body, LineFile lineFile) {
        super(lineFile);

        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public void setParameters(Line parameters) {
        this.parameters = parameters;
    }

    public void setBody(BlockStmt body) {
        this.body = body;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        Function.Parameter[] params = SplCallable.evalParams(parameters, env);

        Function function = new Function(body, params, env, name, getLineFile());
        Pointer funcPtr = env.getMemory().allocateFunction(function, env);

        env.defineFunction(name, funcPtr, getLineFile());
        return funcPtr;
    }

    public Pointer evalAsMethod(Environment classDefEnv) {
        Function.Parameter[] params = SplCallable.insertThis(SplCallable.evalParams(parameters, classDefEnv));

        SplMethod function = new SplMethod(body, params, classDefEnv, name, getLineFile());

        return classDefEnv.getMemory().allocateFunction(function, classDefEnv);
    }

    @Override
    public String toString() {
        if (name == null)
            return String.format("fn(%s): %s", parameters, body);
        else
            return String.format("fn %s(%s): %s", name, parameters, body);
    }

    @Override
    public String reprString() {
        return "fn " + name;
    }

    public Line getParameters() {
        return parameters;
    }

    public BlockStmt getBody() {
        return body;
    }
}
