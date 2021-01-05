package spl.interpreter.splObjects;

import spl.ast.BlockStmt;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.MethodEnvironment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class SplMethod extends Function {

    private Reference classPtr;

    public SplMethod(BlockStmt body,
                     Parameter[] params,
                     Environment classDefEnv,
                     String definedName,
                     LineFilePos lineFile) {
        super(body, params, classDefEnv, definedName, lineFile);
    }

    public void setClassPtr(Reference classPtr) {
        this.classPtr = classPtr;
    }

    /**
     * This method calls a method in an spl instance.
     * <p>
     * Note that {@code evaluatedArguments} has at least one element, which is a reference to "this"
     *
     * @param evaluatedArgs arguments, evaluated
     * @param callingEnv    environment of calling
     * @param argLineFile   line file of argument
     * @return function returns
     */
    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Environment callingEnv, LineFilePos argLineFile) {
        MethodEnvironment scope = new MethodEnvironment(definitionEnv, callingEnv, definedName);
        return callEssential(evaluatedArgs, callingEnv, scope, argLineFile);
    }

    @Override
    public String toString() {
        return "Method " + definedName + ": {" + body.getLines().size() + " lines, defined in " +
                definitionEnv.getMemory().get(classPtr) + "} ";
    }

    public Reference getClassPtr() {
        return classPtr;
    }
}
