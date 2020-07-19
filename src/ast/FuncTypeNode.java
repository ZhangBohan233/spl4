package ast;

import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Function;
import interpreter.types.*;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class FuncTypeNode extends BinaryExpr {

    public FuncTypeNode(LineFile lineFile) {
        super("->", lineFile);
    }

    private boolean isLambdaOperator() {
        return left instanceof LambdaExpr;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (!isLambdaOperator()) throw new TypeError();

        Line parameters = ((LambdaExpr) left).getParameters();

        Function.Parameter[] params = Function.evalParamTypes(parameters, env);

        FunctionEnvironment fakeEnv = new FunctionEnvironment(env, env, "");  // TODO:??
        for (Function.Parameter p : params) fakeEnv.defineVar(p.name, getLineFile());

        Function function = new Function(right, params, env, getLineFile());

//        TypeValue funcTv = new TypeValue(lambdaType, funcPtr);
//        env.defineFunction(name, funcTv, getLineFile());
        return env.getMemory().allocateFunction(function, env);
    }

//    @Override
//    public Type evalType(Environment environment) {
//        if (isLambdaOperator()) throw new TypeError();
//        if (!(right instanceof TypeRepresent)) throw new TypeError();
//        Type rType = ((TypeRepresent) right).evalType(environment);
//        List<Node> paramNodes = ((Line) left).getChildren();
//        List<Type> paramTypes = new ArrayList<>();
//        for (Node node : paramNodes) {
//            if (node instanceof TypeRepresent) {
//                paramTypes.add(((TypeRepresent) node).evalType(environment));
//            } else throw new TypeError();
//        }
//        return new CallableType(paramTypes, rType);
//    }
}
