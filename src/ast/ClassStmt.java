package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import interpreter.primitives.Pointer;
import interpreter.splObjects.Instance;
import interpreter.splObjects.NativeFunction;
import interpreter.splObjects.SplClass;
import interpreter.splObjects.SplObject;
import parser.ParseError;
import util.Constants;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class ClassStmt extends Node {

    private final String className;
    private List<Node> superclassesNodes;
    private final BlockStmt body;

    /**
     * @param className  name of class
     * @param extensions extending line, null if not specified.
     * @param body       body block
     * @param lineFile   line file
     */
    public ClassStmt(String className, Line extensions, BlockStmt body, LineFile lineFile) {
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

        List<Pointer> superclassesPointers = new ArrayList<>();
        for (int i = superclassesNodes.size() - 1; i >= 0; i--) {
            Pointer scPtr = (Pointer) superclassesNodes.get(i).evaluate(env);
            superclassesPointers.add(scPtr);
        }

        SplClass clazz = new SplClass(className, superclassesPointers, body, env);
        Pointer clazzPtr = env.getMemory().allocateObject(clazz, env);

        env.defineVarAndSet(className, clazzPtr, getLineFile());

        String iofName = className + "?";
        NativeFunction instanceOfFunc = new NativeFunction(iofName, 1) {
            @Override
            protected Bool callFunc(SplElement[] evaluatedArgs, Environment callingEnv) {
                SplElement arg = evaluatedArgs[0];
                if (arg instanceof Pointer) {
                    SplObject obj = callingEnv.getMemory().get((Pointer) arg);
                    if (obj instanceof Instance) {
                        Pointer argClazzPtr = ((Instance) obj).getClazzPtr();
                        return Bool.boolValueOf(SplClass.isSuperclassOf(clazzPtr, argClazzPtr, callingEnv.getMemory()));
                    }
                }
                return Bool.FALSE;
            }
        };
        Pointer iofPtr = env.getMemory().allocateFunction(instanceOfFunc, env);
        env.defineVarAndSet(iofName, iofPtr, getLineFile());

        return clazzPtr;
    }

    @Override
    public String toString() {
        return String.format("class %s(%s)", className, superclassesNodes);
//        String title = isInterface ? "Interface" : "Class";
//        if (templateNode == null) {
//            return String.format("%s %s extends %s implements %s %s",
//                    title, className, superclass, implementations, body);
//        } else {
//            return String.format("%s %s %s extends %s implements %s %s",
//                    title, className, templateNode, superclass, implementations, body);
//        }
    }
}
