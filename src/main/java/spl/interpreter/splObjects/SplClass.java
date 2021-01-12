package spl.interpreter.splObjects;

import spl.ast.*;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.env.GlobalEnvironment;
import spl.interpreter.env.ModuleEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splErrors.RuntimeSyntaxError;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.*;

public class SplClass extends SplObject {

    /**
     * Pointer to direct superclasses of this class.
     * <p>
     * This is not equivalent to mro.
     */
    private final List<Reference> superclassPointers;

    private final String className;
    private final Environment definitionEnv;
    private final List<Node> classNodes = new ArrayList<>();
    private final Map<String, Reference> methodPointers = new HashMap<>();
    // mro array used by java
    private Reference[] mro;
    // mro array used by spl
    private Reference mroArrayPointer;
    private Reference classNameRef;

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
    private SplClass(String className, List<Reference> superclassPointers,
                     BlockStmt body, Environment definitionEnv) {
        this.className = className;
        this.superclassPointers = superclassPointers;
        this.definitionEnv = definitionEnv;

        evalBody(body);
        checkConstructor();
    }

    public static Reference createClassAndAllocate(String className, List<Reference> superclassPointers,
                                                   BlockStmt body, Environment definitionEnv) {

        SplClass clazz = new SplClass(className, superclassPointers, body, definitionEnv);
        Reference clazzPtr = definitionEnv.getMemory().allocateObject(clazz, definitionEnv);
        clazz.makeMro(clazzPtr);
        clazz.updateMethods(clazzPtr);

        return clazzPtr;
    }

    public static boolean isSuperclassOf(Reference superclassPtr, SplElement childClassEle, Memory memory) {
        if (childClassEle.valueEquals(superclassPtr)) return true;
        if (childClassEle instanceof Reference) {
            Reference childClassPtr = (Reference) childClassEle;
            SplObject splObject = memory.get(childClassPtr);
            if (splObject instanceof SplClass) {
                SplClass childClazz = (SplClass) splObject;
                for (Reference supPtr : childClazz.mro) {
                    if (superclassPtr.valueEquals(supPtr)) return true;
                }
            }
        }
        return false;
    }

    private void updateMethods(Reference clazzPtr) {
        for (Reference methodPtr : methodPointers.values()) {
            SplMethod method = (SplMethod) definitionEnv.getMemory().get(methodPtr);
            method.setClassPtr(clazzPtr);
            checkOverride(method);
        }
    }

    private void checkOverride(SplMethod method) {
        if (method.definedName.equals(Constants.CONSTRUCTOR)) return;
        for (int i = 1; i < mro.length; i++) {
            Reference scRef = mro[i];
            SplClass sc = (SplClass) definitionEnv.getMemory().get(scRef);
            Reference scMethodRef = sc.methodPointers.get(method.definedName);
            if (scMethodRef != null) {
                SplMethod scMethod = (SplMethod) definitionEnv.getMemory().get(scMethodRef);
                // System.out.println("Override! " + method.definedName + " from " + className + " to " + sc.className);
                if (scMethod.params.length != method.params.length) {
                    SplInvokes.throwException(
                            definitionEnv,
                            Constants.INHERITANCE_ERROR,
                            String.format(
                                    "Method %s in class %s has different number of parameters from its " +
                                            "overriding method in class %s.",
                                    method.definedName,
                                    getFullName(),
                                    sc.getFullName()),
                            method.lineFile
                    );
                }
                return;
            }
        }
    }

    /**
     * Makes the method resolution order array, ranked from nearest to farthest.
     * <p>
     * This first element is always this class.
     *
     * @param thisClazzPtr pointer to this class
     */
    private void makeMro(Reference thisClazzPtr) {
        List<Reference> mro = new ArrayList<>();
        fillMro(mro, thisClazzPtr);
        reduceMro(mro);
        checkValidMro(mro);

        this.mro = mro.toArray(new Reference[0]);
        this.mroArrayPointer = SplArray.createArray(SplElement.POINTER, this.mro.length, definitionEnv);
        for (int i = 0; i < this.mro.length; i++) {
            SplArray.setItemAtIndex(mroArrayPointer, i, this.mro[i], definitionEnv, LineFilePos.LF_INTERPRETER);
        }
    }

    private void fillMro(List<Reference> mro, Reference thisClassPtr) {
        mro.add(thisClassPtr);
        for (Reference scPtr : superclassPointers) {
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
    private void reduceMro(List<Reference> mro) {
        Set<Reference> superclasses = new HashSet<>();
        ListIterator<Reference> reverseIterator = mro.listIterator(mro.size());
        while (reverseIterator.hasPrevious()) {
            Reference ptr = reverseIterator.previous();
            if (superclasses.contains(ptr)) {
                reverseIterator.remove();
            } else {
                superclasses.add(ptr);
            }
        }
    }

    private void checkValidMro(List<Reference> mro) {

    }

    public Reference[] getMro() {
        return mro;
    }

    private void evalBody(BlockStmt body) {
        for (Line line : body.getLines()) {
            for (Node lineNode : line.getChildren()) {
                if (lineNode instanceof Declaration || lineNode instanceof Assignment) {
                    classNodes.add(lineNode);
                } else if (lineNode instanceof FuncDefinition) {
                    FuncDefinition fd = (FuncDefinition) lineNode;
                    Reference methodPtr = fd.evalAsMethod(definitionEnv);
                    methodPointers.put(fd.name.getName(), methodPtr);
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
                    new NameNode(Constants.CONSTRUCTOR, LineFilePos.LF_INTERPRETER),
                    new Line(),
                    new BlockStmt(LineFilePos.LF_INTERPRETER),
                    LineFilePos.LF_INTERPRETER);

            Reference constructorPtr = fd.evalAsMethod(definitionEnv);
            methodPointers.put(Constants.CONSTRUCTOR, constructorPtr);
        }
    }

    public Map<String, Reference> getMethodPointers() {
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

    public String getFullName() {
        if (definitionEnv instanceof ModuleEnvironment) {
            return ((ModuleEnvironment) definitionEnv).getModuleName() + "$" + getClassName();
        } else {
            return getClassName();
        }
    }

    public SplElement getAttr(Reference selfPtr, Node attrNode, Environment env, LineFilePos lineFile) {
        if (attrNode instanceof NameNode) {
            String name = ((NameNode) attrNode).getName();
            if (name.equals(Constants.CLASS_NAME)) {
                if (classNameRef == null) {
                    classNameRef = StringLiteral.createString(className.toCharArray(), env, lineFile);
                }
                return classNameRef;
            } else if (name.equals(Constants.CLASS_MRO)) {
                return mroArrayPointer;
            }
        }
        SplInvokes.throwException(
                env,
                Constants.ATTRIBUTE_EXCEPTION,
                "Class does not have attribute '" + attrNode + "'. ",
                lineFile
        );
        return Undefined.ERROR;
    }

    /**
     * This method is used for gc.
     *
     * @return a list containing all pointers that should not be collected by gc.
     */
    public List<Reference> getAllAttrPointers() {
        List<Reference> res = new ArrayList<>();
        res.add(mroArrayPointer);
        res.addAll(methodPointers.values());
        return res;
    }

    @Override
    public List<Reference> listAttrReferences() {
        List<Reference> refs = new ArrayList<>();
        refs.addAll(superclassPointers);
        refs.addAll(methodPointers.values());
        refs.addAll(Arrays.asList(mro));
        refs.add(mroArrayPointer);
        if (classNameRef != null) refs.add(classNameRef);
        return refs;
    }

    @Override
    public String toString() {
        return "Class " + className + " ";
    }
}
