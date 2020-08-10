package interpreter.splObjects;

import ast.BlockStmt;
import interpreter.EvaluatedArguments;
import interpreter.env.Environment;
import interpreter.env.FunctionEnvironment;
import interpreter.env.MethodEnvironment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import util.LineFile;

public class SplMethod extends Function {

    private Pointer classPtr;

    public SplMethod(BlockStmt body,
                     Parameter[] params,
                     Environment classDefEnv,
                     String definedName,
                     LineFile lineFile) {
        super(body, params, classDefEnv, definedName, lineFile);
    }

    public void setClassPtr(Pointer classPtr) {
        this.classPtr = classPtr;
    }

    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFile argLineFile) {
        MethodEnvironment scope = new MethodEnvironment(definitionEnv, callingEnv, definedName);
        return callEssential(evaluatedArgs, callingEnv, scope, argLineFile);
    }

    @Override
    public String toString() {
        return "Method " + definedName + ": {" + body.getLines().size() + " lines, defined in " +
                definitionEnv.getMemory().get(classPtr) + "} ";
    }
}
