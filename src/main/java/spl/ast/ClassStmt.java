package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.NativeFunction;
import spl.interpreter.splObjects.SplClass;
import spl.interpreter.splObjects.SplObject;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.ArrayList;
import java.util.List;

public class ClassStmt extends Expression {

    private final String className;
    private List<Node> superclassesNodes;
    private final BlockStmt body;

    /**
     * @param className  name of class
     * @param extensions extending line, null if not specified.
     * @param body       body block
     * @param lineFile   line file
     */
    public ClassStmt(String className, Line extensions, BlockStmt body, LineFilePos lineFile) {
        super(lineFile);

        this.className = className;
        this.superclassesNodes = extensions == null ? null : extensions.getChildren();
        this.body = body;
    }

    private void validateExtending() {
        if (superclassesNodes == null) {
            superclassesNodes = new ArrayList<>();
            if (!className.equals(Constants.OBJECT_CLASS)) {
                superclassesNodes.add(new NameNode("Object", getLineFile()));
            }
        }
    }

    @Override
    protected SplElement internalEval(Environment env) {

        validateExtending();

        List<Reference> superclassesPointers = new ArrayList<>();
        for (Node superclassesNode : superclassesNodes) {
            Reference scPtr = (Reference) superclassesNode.evaluate(env);
            superclassesPointers.add(scPtr);
        }

        Reference clazzPtr = SplClass.createClassAndAllocate(className, superclassesPointers, body, env);

        env.defineVarAndSet(className, clazzPtr, getLineFile());

        String iofName = className + "?";
        NativeFunction instanceOfFunc = new NativeFunction(iofName, 1) {
            @Override
            protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs.positionalArgs.get(0);
                if (arg instanceof Reference) {
                    SplObject obj = callingEnv.getMemory().get((Reference) arg);
                    if (obj instanceof Instance) {
                        Reference argClazzPtr = ((Instance) obj).getClazzPtr();
                        return Bool.boolValueOf(SplClass.isSuperclassOf(clazzPtr, argClazzPtr, callingEnv.getMemory()));
                    }
                }
                return Bool.FALSE;
            }
        };
        Reference iofPtr = env.getMemory().allocateFunction(instanceOfFunc, env);
        env.defineVarAndSet(iofName, iofPtr, getLineFile());

        return clazzPtr;
    }

    @Override
    public String toString() {
        return String.format("class %s(%s)", className, superclassesNodes);
    }

    @Override
    public String reprString() {
        return "class " + className;
    }

    public List<Node> getSuperclassesNodes() {
        return superclassesNodes;
    }

    public BlockStmt getBody() {
        return body;
    }
}
