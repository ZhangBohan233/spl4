package spl.interpreter.splObjects;

import spl.ast.BlockStmt;
import spl.ast.StringLiteralRef;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.MethodEnvironment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

import java.util.List;

public class SplMethod extends Function {

    public final int methodDefClassId;
    private Reference classPtr;

    public SplMethod(BlockStmt body,
                     Parameter[] params,
                     Environment classDefEnv,
                     String definedName,
                     StringLiteralRef docRef,
                     int methodDefClassId,
                     LineFilePos lineFile) {
        super(body, params, classDefEnv, definedName, docRef, lineFile);

        this.methodDefClassId = methodDefClassId;
    }

    @Override
    public List<Reference> listAttrReferences() {
        return classPtr != null ? List.of(classPtr) : List.of();
    }

    /**
     * This method calls a method in an spl instance.
     * <p>
     * Note that {@code evaluatedArguments} has at least one element, which is a reference to "this"
     *
     * @param evaluatedArgs arguments, evaluated
     * @param generics      actual generics, evaluated
     * @param callingEnv    environment of calling
     * @param argLineFile   line file of argument
     * @return function returns
     */
    @Override
    public SplElement call(EvaluatedArguments evaluatedArgs, Reference[] generics,
                           Environment callingEnv, LineFilePos argLineFile) {
        MethodEnvironment scope = new MethodEnvironment(definitionEnv, callingEnv, definedName, methodDefClassId);
        return callEssential(evaluatedArgs, generics, callingEnv, scope, argLineFile);
    }

    @Override
    public String toString() {
        return String.format("Method %s: {%d lines, defined in %s. %s}",
                definedName,
                body.getLines().size(),
                definitionEnv.getMemory().get(classPtr),
                lineFile);
    }

    public Reference getClassPtr() {
        return classPtr;
    }

    public void setClassPtr(Reference classPtr) {
        this.classPtr = classPtr;
    }

    @Override
    public String getName() {
        return ((SplClass) definitionEnv.getMemory().get(classPtr)).getClassName() + "." + definedName;
    }
}
