package ast;

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
    protected SplElement internalEval(Environment env) {

        Function.Parameter[] params = SplCallable.evalParams(parameters, env);

        Function function = new Function(body, params, env, name, getLineFile());
        Pointer funcPtr = env.getMemory().allocateFunction(function, env);

        env.defineFunction(name, funcPtr, getLineFile());
        return funcPtr;
    }

    public Pointer evalAsMethod(Environment classDefEnv) {
        Function.Parameter[] params = SplCallable.insertThis(SplCallable.evalParams(parameters, classDefEnv));

        Method function = new Method(body, params, classDefEnv, name, getLineFile());

        return classDefEnv.getMemory().allocateFunction(function, classDefEnv);
    }

    @Override
    public String toString() {
        if (name == null)
            return String.format("fn(%s): %s", parameters, body);
        else
            return String.format("fn %s(%s): %s", name, parameters, body);
    }

    public boolean doesOverride(FuncDefinition superMethod, Environment env) {
        return true;
//        if (parameters.getChildren().size() != superMethod.parameters.getChildren().size()) return false;
//        for (int i = 0 ; i < parameters.getChildren().size(); ++i) {
//            Node thisParam = parameters.getChildren().get(i);
//            Node superParam = superMethod.parameters.getChildren().get(i);
//            // TODO: check this. Two cases: Declaration and Assignment
//        }
//
//        Type thisRType = rType.evalType(env);
//        Type superRType = superMethod.rType.evalType(env);
//        return superRType.isSuperclassOfOrEquals(thisRType, env);
    }
}
