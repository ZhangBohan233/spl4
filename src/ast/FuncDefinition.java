package ast;

import interpreter.primitives.SplElement;
import interpreter.splObjects.Function;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import parser.ParseError;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class FuncDefinition extends Node {

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

        Function.Parameter[] params = Function.evalParamTypes(parameters, env);
//        CallableType funcType = new CallableType();

        Function function = new Function(body, params, env, name, getLineFile());
        Pointer funcPtr = env.getMemory().allocateFunction(function, env);

//        TypeValue funcTv = new TypeValue(funcType, funcPtr);
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
