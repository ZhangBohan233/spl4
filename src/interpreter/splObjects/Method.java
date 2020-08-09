package interpreter.splObjects;

import ast.BlockStmt;
import interpreter.EvaluatedArguments;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.env.InstanceEnvironment;
import interpreter.primitives.SplElement;
import interpreter.splErrors.NativeError;
import util.LineFile;

public class Method extends Function {

    public Method(BlockStmt body, Parameter[] parameters, String definedName, LineFile lineFile) {
        super(body, parameters, null, definedName, lineFile);
    }

    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile lineFile) {
        throw new NativeError();
    }

    public SplElement methodCall(EvaluatedArguments evaluatedArgs,
                                 Environment callingEnv,
                                 InstanceEnvironment instanceEnv,
                                 LineFile lineFile) {
        FunctionEnvironment scope = new FunctionEnvironment(instanceEnv, callingEnv, definedName);
        return callEssential(evaluatedArgs, callingEnv, scope, lineFile);
    }
}
