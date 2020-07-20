package interpreter.splObjects;

import ast.BlockStmt;
import ast.Node;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;

import java.util.List;

public class SplClass extends SplObject {

    /**
     * Pointer to superclasses, ordered from closet to furthest.
     * <p>
     * For instance, a pointer to class 'Object' is always the last element in this list
     */
    private final List<Pointer> superclassPointers;

    private final BlockStmt body;
    private final String className;
    private final Environment definitionEnv;
//    public final boolean isAbstract;
//    public final boolean isInterface;

    public SplClass(String className, List<Pointer> superclassPointers,
                    BlockStmt body, Environment definitionEnv) {
        this.className = className;
        this.superclassPointers = superclassPointers;
        this.body = body;
        this.definitionEnv = definitionEnv;
    }

    public List<Pointer> getSuperclassPointers() {
        return superclassPointers;
    }

    public BlockStmt getBody() {
        return body;
    }

    public Environment getDefinitionEnv() {
        return definitionEnv;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "Class <" + className + ">";
    }
}
