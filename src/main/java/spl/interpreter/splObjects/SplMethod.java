package spl.interpreter.splObjects;

import spl.ast.BlockStmt;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.MethodEnvironment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public class SplMethod extends Function {

    private Reference classPtr;

    public SplMethod(BlockStmt body,
                     Parameter[] params,
                     Environment classDefEnv,
                     String definedName,
                     LineFile lineFile) {
        super(body, params, classDefEnv, definedName, lineFile);
    }

    public void setClassPtr(Reference classPtr) {
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
