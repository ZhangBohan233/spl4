package ast;

import interpreter.Memory;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Function;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.splObjects.Method;
import interpreter.splObjects.SplCallable;
import util.LineFile;

public class FuncDefinition extends AbstractExpression {

    public final String name;
//    private TypeRepresent rType;
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
    protected Pointer internalEval(Environment env) {
        Function.Parameter[] params = SplCallable.evalParams(parameters, env);

        Function function = new Function(body, params, env, name, getLineFile());
        Pointer funcPtr = env.getMemory().allocateFunction(function, env);

        env.defineFunction(name, funcPtr, getLineFile());
        return funcPtr;
    }

    @Override
    public String toString() {
        if (name == null)
            return String.format("fn(%s): %s", parameters, body);
        else
            return String.format("fn %s(%s): %s", name, parameters, body);
    }

    public static Pointer evalMethod(FuncDefinition definition, Environment classDefEnv) {
        Function.Parameter[] params = SplCallable.insertThisToParam(
                SplCallable.evalParams(definition.parameters, classDefEnv));

        Method method = new Method(definition.body, params, definition.name, classDefEnv, definition.lineFile);

        return classDefEnv.getMemory().allocateFunction(method, classDefEnv);
    }
}
