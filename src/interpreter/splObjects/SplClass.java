package interpreter.splObjects;

import ast.BlockStmt;
import interpreter.Memory;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;

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

    public static boolean isSuperclassOf(Pointer superclassPtr, SplElement childClassEle, Memory memory) {
        if (childClassEle instanceof Pointer) {
            Pointer childClassPtr = (Pointer) childClassEle;
            if (superclassPtr.getPtr() == childClassPtr.getPtr()) return true;
            SplObject splObject = memory.get(childClassPtr);
            if (splObject instanceof SplClass) {
                SplClass childClazz = (SplClass) splObject;
                for (Pointer supPtr : childClazz.superclassPointers) {
                    if (isSuperclassOf(superclassPtr, supPtr, memory)) return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Class <" + className + ">";
    }
}
