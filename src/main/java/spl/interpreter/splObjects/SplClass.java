package spl.interpreter.splObjects;

import spl.ast.*;
import spl.interpreter.splErrors.AttributeError;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.RuntimeSyntaxError;
import spl.util.Constants;
import spl.util.LineFile;

import java.util.*;

public class SplClass extends SplObject {

    /**
     * Pointer to superclasses, ordered from closet to furthest.
     * <p>
     * For instance, a pointer to class 'Object' is always the last element in this list
     */
    private final List<Pointer> superclassPointers;

    private final String className;
    private final Environment definitionEnv;

    // mro array used by java
    private Pointer[] mro;

    // mro array used by spl
    private Pointer mroArrayPointer;

    private final List<Node> classNodes = new ArrayList<>();
    private final Map<String, Pointer> methodPointers = new HashMap<>();

    /**
     * The private constructor, only for in-class call.
     * <p>
     * This is because this constructor only creates the instance, but does not allocate in the memory.
     * So the mandatory field 'mro' cannot be created due to the lack of class pointer.
     *
     * @param className          name of class
     * @param superclassPointers pointer to direct superclass
     * @param body               class body
     * @param definitionEnv      environment of definition
     */
    private SplClass(String className, List<Pointer> superclassPointers,
                     BlockStmt body, Environment definitionEnv) {
        this.className = className;
        this.superclassPointers = superclassPointers;
        this.definitionEnv = definitionEnv;

        evalBody(body);
        checkConstructor();
    }

    public static Pointer createClassAndAllocate(String className, List<Pointer> superclassPointers,
                                                 BlockStmt body, Environment definitionEnv) {

        SplClass clazz = new SplClass(className, superclassPointers, body, definitionEnv);
        Pointer clazzPtr = definitionEnv.getMemory().allocateObject(clazz, definitionEnv);
        clazz.makeMro(clazzPtr);
        clazz.updateMethods(clazzPtr);

        return clazzPtr;
    }

    private void updateMethods(Pointer clazzPtr) {
        for (Pointer methodPtr : methodPointers.values()) {
            SplMethod method = (SplMethod) definitionEnv.getMemory().get(methodPtr);
            method.setClassPtr(clazzPtr);
        }
    }

    private void makeMro(Pointer thisClazzPtr) {
        List<Pointer> mro = new ArrayList<>();
        fillMro(mro, thisClazzPtr);
        reduceMro(mro);
        checkValidMro(mro);

        this.mro = mro.toArray(new Pointer[0]);
        this.mroArrayPointer = SplArray.createArray(SplElement.POINTER, this.mro.length, definitionEnv);
        for (int i = 0; i < this.mro.length; i++) {
            SplArray.setItemAtIndex(mroArrayPointer, i, this.mro[i], definitionEnv, LineFile.LF_INTERPRETER);
        }
    }

    private void fillMro(List<Pointer> mro, Pointer thisClassPtr) {
        mro.add(thisClassPtr);
        for (Pointer scPtr : superclassPointers) {
            SplClass sc = (SplClass) definitionEnv.getMemory().get(scPtr);
            sc.fillMro(mro, scPtr);
        }
    }

    /**
     * Modified the mro list to removes the duplicated mro.
     * <p>
     * For example, [D -> B -> A -> Object -> C -> A -> Object] will be reduced to [D -> B -> C -> A -> Object]
     *
     * @param mro the mro list to be modified.
     */
    private void reduceMro(List<Pointer> mro) {
        Set<Pointer> superclasses = new HashSet<>();
        ListIterator<Pointer> reverseIterator = mro.listIterator(mro.size());
        while (reverseIterator.hasPrevious()) {
            Pointer ptr = reverseIterator.previous();
            if (superclasses.contains(ptr)) {
                reverseIterator.remove();
            } else {
                superclasses.add(ptr);
            }
        }
    }

    private void checkValidMro(List<Pointer> mro) {

    }

    public Pointer[] getMro() {
        return mro;
    }

    private void evalBody(BlockStmt body) {
        for (Line line : body.getLines()) {
            for (Node lineNode : line.getChildren()) {
                if (lineNode instanceof Declaration || lineNode instanceof Assignment) {
                    classNodes.add(lineNode);
                } else if (lineNode instanceof FuncDefinition) {
                    FuncDefinition fd = (FuncDefinition) lineNode;
                    Pointer methodPtr = fd.evalAsMethod(definitionEnv);
                    methodPointers.put(fd.name, methodPtr);
                } else if (lineNode instanceof ContractNode) {
                    ((ContractNode) lineNode).evalAsMethod(methodPointers, className, definitionEnv);
                } else
                    throw new RuntimeSyntaxError("Invalid class body. ", line.lineFile);
            }
        }
    }

    private void checkConstructor() {
        if (!methodPointers.containsKey(Constants.CONSTRUCTOR)) {
            // If class no constructor, put an empty default constructor
            FuncDefinition fd = new FuncDefinition(
                    Constants.CONSTRUCTOR,
                    new Line(),
                    new BlockStmt(LineFile.LF_INTERPRETER),
                    LineFile.LF_INTERPRETER);

            Pointer constructorPtr = fd.evalAsMethod(definitionEnv);
            methodPointers.put(Constants.CONSTRUCTOR, constructorPtr);
        }
    }

//    public List<Pointer> getSuperclassPointers() {
//        return superclassPointers;
//    }

    public Map<String, Pointer> getMethodPointers() {
        return methodPointers;
    }

    public List<Node> getClassNodes() {
        return classNodes;
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

    public SplElement getAttr(Pointer selfPtr, Node attrNode, Environment env, LineFile lineFile) {
        if (attrNode instanceof NameNode) {
            String name = ((NameNode) attrNode).getName();
            if (name.equals(Constants.CLASS_NAME)) {
                return StringLiteral.createString(className.toCharArray(), env, lineFile);
            } else if (name.equals(Constants.CLASS_MRO)) {
                return mroArrayPointer;
            }
        }
        throw new AttributeError("Class does not have attribute '" + attrNode + "'. ", lineFile);
    }

    /**
     * This method is used for gc.
     * @return a list containing all pointers that should not be collected by gc.
     *
     */
    public List<Pointer> getAllAttrPointers() {
        List<Pointer> res = new ArrayList<>();
        res.add(mroArrayPointer);
        res.addAll(methodPointers.values());
        return res;
    }

    @Override
    public String toString() {
        return "Class " + className + " ";
    }

}
