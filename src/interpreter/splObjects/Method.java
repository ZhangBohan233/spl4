package interpreter.splObjects;

import ast.BlockStmt;
import interpreter.EvaluatedArguments;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.env.MethodEnvironment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class Method extends Function {

    public Method(BlockStmt body, Parameter[] params, Environment definitionEnv, String definedName, LineFile lineFile) {
        super(body, params, definitionEnv, definedName, lineFile);
    }

    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile argLineFile) {
        MethodEnvironment scope = new MethodEnvironment(definitionEnv, callingEnv, definedName);
        return callEssential(evaluatedArgs, callingEnv, scope, argLineFile);
    }
}
