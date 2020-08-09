package interpreter.splObjects;

import ast.BlockStmt;
import interpreter.EvaluatedArguments;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.env.InstanceEnvironment;
import interpreter.env.MethodEnvironment;
import interpreter.primitives.SplElement;
import interpreter.splErrors.NativeError;
import util.LineFile;

public class Method extends Function {

    public Method(BlockStmt body, Parameter[] parameters, String definedName, Environment classDefEnv, LineFile lineFile) {
        super(body, parameters, classDefEnv, definedName, lineFile);
    }

    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile lineFile) {
        throw new NativeError();
    }

    public SplElement methodCall(EvaluatedArguments evaluatedArgs,
                                 Environment callingEnv,
                                 InstanceEnvironment instanceEnv,
                                 LineFile lineFile) {
        MethodEnvironment scope = new MethodEnvironment(definitionEnv, callingEnv, definedName);
        EvaluatedArguments.insertThisPtr(evaluatedArgs, callingEnv.getMemory().getCurrentThisPtr());
        return callEssential(evaluatedArgs, callingEnv, scope, lineFile);
    }
}
