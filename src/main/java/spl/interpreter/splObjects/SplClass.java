package spl.interpreter.splObjects;

import spl.ast.*;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.env.ModuleEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.util.Accessible;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.*;

public class SplClass extends NativeObject {

    private static int classCount = 0;

    /**
     * A unique identifier of this class
     */
    public final int classId = classCount++;
    /**
     * Pointer to direct superclasses of this class.
     * <p>
     * This is not equivalent to mro.
     */
    private final List<Reference> superclassPointers;
    private final Map<Reference, Line> superclassGenerics;

    private final String className;
    private final Environment definitionEnv;
    private final LinkedHashMap<String, Node> fieldNodes = new LinkedHashMap<>();
    private final Map<String, Reference> methodPointers = new HashMap<>();
    private final String[] templates;
    private final StringLiteralRef docRef;
    // mro array used by spl
    @Accessible
    Reference __mro__;
    // mro array used by java
    // the first is the self class
    private Reference[] mroArray;
    private Reference classNameRef;
    private String mroErrorMsg = "";

    /**
     * The private constructor, only for in-class call.
     * <p>
     * This is because this constructor only creates the instance, but does not allocate in the memory.
     * So the mandatory field 'mro' cannot be created due to the lack of class pointer.
     *
     * @param className          name of class
     * @param superclassPointers pointer to direct superclass
     * @param templates          defined template names
     * @param body               class body
     * @param definitionEnv      environment of definition
     * @param docRef             string literal reference of docstring
     */
    private SplClass(String className,
                     List<Reference> superclassPointers,
                     String[] templates,
                     Map<Reference, Line> superclassGenerics,
                     BlockStmt body,
                     Environment definitionEnv,
                     StringLiteralRef docRef) {
        this.className = className;
        this.superclassPointers = superclassPointers;
        this.templates = templates;
        this.superclassGenerics = superclassGenerics;
        this.definitionEnv = definitionEnv;
        this.docRef = docRef;

        evalBody(body, definitionEnv);
        checkConstructor();
    }

    public static SplElement createClassAndAllocate(String className,
                                                    List<Reference> superclassPointers,
                                                    String[] templates,
                                                    Map<Reference, Line> superclassGenerics,
                                                    BlockStmt body,
                                                    Environment definitionEnv,
                                                    StringLiteralRef docRef,
                                                    LineFilePos lineFilePos) {
        if (superclassGenerics != null) {
            for (Map.Entry<Reference, Line> entry : superclassGenerics.entrySet()) {
                SplClass sc = definitionEnv.getMemory().get(entry.getKey());
                if (sc.getTemplates() == null || sc.getTemplates().length != entry.getValue().size()) {
                    return SplInvokes.throwExceptionWithError(
                            definitionEnv,
                            Constants.INHERITANCE_ERROR,
                            String.format("Unequal generic length of class '%s' with superclass '%s'.",
                                    className, sc.className),
                            lineFilePos
                    );
                }
            }
        }

        SplClass clazz = new SplClass(className, superclassPointers, templates, superclassGenerics,
                body, definitionEnv, docRef);
        if (definitionEnv.hasException()) return Undefined.ERROR;

        Reference clazzPtr = definitionEnv.getMemory().allocateObject(clazz, definitionEnv);

        definitionEnv.getMemory().addTempPtr(clazzPtr);

        if (!clazz.makeMro(clazzPtr, lineFilePos)) return Undefined.ERROR;
        clazz.updateMethods(clazzPtr);

        definitionEnv.getMemory().removeTempPtr(clazzPtr);

        return clazzPtr;
    }

    public boolean isSuperclassOf(SplElement childClassEle, Memory memory) {
        if (childClassEle instanceof Reference) {
            SplObject childClassObj = memory.get((Reference) childClassEle);
            if (childClassObj instanceof SplClass) {
                for (Reference supPtr : ((SplClass) childClassObj).mroArray) {
                    SplClass supClass = memory.get(supPtr);
                    if (supClass == this) return true;
                }
            }
        }
        return false;
    }

    public static boolean isSuperclassOf(Reference superclassPtr, SplElement childClassEle, Memory memory) {
        if (childClassEle.equals(superclassPtr)) return true;
        if (childClassEle instanceof Reference) {
            Reference childClassPtr = (Reference) childClassEle;
            SplObject splObject = memory.get(childClassPtr);
            if (splObject instanceof SplClass) {
                SplClass childClazz = (SplClass) splObject;
                for (Reference supPtr : childClazz.mroArray) {
                    if (superclassPtr.equals(supPtr)) return true;
                }
            }
        }
        return false;
    }

    private void updateMethods(Reference clazzPtr) {
        for (Reference methodPtr : methodPointers.values()) {
            SplMethod method = definitionEnv.getMemory().get(methodPtr);
            method.setClassPtr(clazzPtr);
            checkOverride(method);
        }
    }

    private void checkOverride(SplMethod method) {
        if (method.definedName.equals(Constants.CONSTRUCTOR)) return;
        for (int i = 1; i < mroArray.length; i++) {
            Reference scRef = mroArray[i];
            SplClass sc = definitionEnv.getMemory().get(scRef);
            Reference scMethodRef = sc.methodPointers.get(method.definedName);
            if (scMethodRef != null) {
                SplMethod scMethod = definitionEnv.getMemory().get(scMethodRef);
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
     * @param lineFilePos  line file pos
     * @return {@code true} if mro successfully made, {@code false otherwise}
     */
    private boolean makeMro(Reference thisClazzPtr, LineFilePos lineFilePos) {
        List<Reference> mro = c3mro(thisClazzPtr);
        if (mro == null) {
            SplInvokes.throwException(
                    definitionEnv,
                    Constants.INHERITANCE_ERROR,
                    String.format("Failed when creating class '%s': %s",
                            className, mroErrorMsg),
                    lineFilePos
            );
            return false;
        }

        this.mroArray = mro.toArray(new Reference[0]);
        this.__mro__ = SplArray.createArray(SplElement.POINTER, this.mroArray.length, definitionEnv);
        for (int i = 0; i < this.mroArray.length; i++) {
            SplArray.setItemAtIndex(__mro__, i, this.mroArray[i], definitionEnv, LineFilePos.LF_INTERPRETER);
        }
        return true;
    }

    private List<Reference> c3mro(Reference clazzPtr) {
        return linearize(clazzPtr);
    }

    private List<Reference> linearize(Reference clazzPtr) {
        SplClass clazz = definitionEnv.getMemory().get(clazzPtr);
        if (clazz.getClassName().equals(Constants.OBJECT_CLASS)) {
            List<Reference> objList = new ArrayList<>();
            objList.add(clazzPtr);
            return objList;  // Do not use List.of(classPtr) because this list must be mutable
        }
        List<List<Reference>> toMerge = new ArrayList<>();
        for (Reference sc : clazz.superclassPointers) {
            toMerge.add(linearize(sc));
        }
        List<Reference> lastList = new ArrayList<>(clazz.superclassPointers);
        toMerge.add(lastList);

        List<Reference> rtn = new ArrayList<>();
        rtn.add(clazzPtr);
        List<Reference> merged = merge(toMerge);
        if (merged == null) return null;  // error
        rtn.addAll(merged);
        return rtn;
    }

    private List<Reference> merge(List<List<Reference>> list) {
        List<Reference> output = new ArrayList<>();
        int headIndex = 0;
        while (!list.isEmpty()) {
            if (headIndex == list.size()) {
                mroErrorMsg = "Inconsistent method resolution.";
                return null;  // indicator of error
            }
            boolean found = false;
            Reference head = list.get(headIndex).get(0);
            MID_LOOP:
            for (int i = 0; i < list.size(); i++) {
                if (i != headIndex) {
                    List<Reference> subList = list.get(i);
                    for (int j = 1; j < subList.size(); j++) {
                        if (subList.get(j).equals(head)) {
                            found = true;
                            break MID_LOOP;
                        }
                    }
                }
            }
            if (found) {
                headIndex++;
            } else {
                Iterator<List<Reference>> iter = list.iterator();
                int rmCount = 0;
                while (iter.hasNext()) {
                    List<Reference> next = iter.next();
                    if (next.remove(head)) rmCount++;
                    if (next.isEmpty()) iter.remove();
                }
                if (rmCount > 2) {  // multiple inheritance
                    SplClass clazz = definitionEnv.getMemory().get(head);
                    if (clazz.getTemplates() != null) {
                        mroErrorMsg = "class '" + clazz.className + "' is a generic class, but is multiple inherited.";
                        return null;
                    }
                }
                output.add(head);
                headIndex = 0;
            }
        }
        return output;
    }

    public Reference[] getMroArray() {
        return mroArray;
    }

    private void evalBody(BlockStmt body, Environment defEnv) {
        for (Line line : body.getLines()) {
            for (Node lineNode : line.getChildren()) {
                if (!evalOneNode(lineNode)) {
                    if (!defEnv.hasException()) {
                        SplInvokes.throwException(
                                defEnv,
                                Constants.RUNTIME_SYNTAX_ERROR,
                                "Invalid class body.",
                                lineNode.getLineFile()
                        );
                    }
                }
            }
        }
    }

    private boolean evalOneNode(Node lineNode) {
        if (lineNode instanceof Declaration) {
            Declaration dec = (Declaration) lineNode;
            fieldNodes.put(dec.declaredName, dec);
        } else if (lineNode instanceof QuickAssignment) {
            QuickAssignment qa = (QuickAssignment) lineNode;
            Node left = qa.getLeft();
            if (!(left instanceof NameNode)) return false;
            fieldNodes.put(((NameNode) left).getName(), qa);
        } else if (lineNode instanceof Assignment) {
            Assignment ass = (Assignment) lineNode;
            Node left = ass.getLeft();
            if (!(left instanceof Declaration)) return false;
            fieldNodes.put(((Declaration) left).declaredName, ass);
        } else if (lineNode instanceof FuncDefinition) {
            FuncDefinition fd = (FuncDefinition) lineNode;
            SplElement mp = fd.evalAsMethod(definitionEnv, classId);
            if (definitionEnv.hasException()) return false;
            Reference methodPtr = (Reference) mp;
            methodPointers.put(fd.name.getName(), methodPtr);
        } else if (lineNode instanceof ContractNode) {
            ((ContractNode) lineNode).evalAsMethod(methodPointers, className, definitionEnv);
        } else {
            return false;
        }
        return true;
    }

    private void checkConstructor() {
        if (!methodPointers.containsKey(Constants.CONSTRUCTOR)) {
            // If class no constructor, put an empty default constructor
            FuncDefinition fd = new FuncDefinition(
                    new NameNode(Constants.CONSTRUCTOR, LineFilePos.LF_INTERPRETER),
                    new Line(),
                    new BlockStmt(LineFilePos.LF_INTERPRETER),
                    null,
                    null,
                    LineFilePos.LF_INTERPRETER);

            SplElement cp = fd.evalAsMethod(definitionEnv, classId);
            if (definitionEnv.hasException()) return;
            Reference constructorPtr = (Reference) cp;
            methodPointers.put(Constants.CONSTRUCTOR, constructorPtr);
        }
    }

    public Map<String, Reference> getMethodPointers() {
        return methodPointers;
    }

    public LinkedHashMap<String, Node> getFieldNodes() {
        return fieldNodes;
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

    public String[] getTemplates() {
        return templates;
    }

    public Map<Reference, Line> getSuperclassGenerics() {
        return superclassGenerics;
    }

    @Accessible
    public SplElement __name__(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 0, "Class.__name__", env, lineFilePos);

        if (classNameRef == null) {
            classNameRef = StringLiteral.createString(className.toCharArray(), env, lineFilePos);
        }
        return classNameRef;
    }

    @Accessible
    public SplElement __doc__(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 0, "Class.__doc__", env, lineFilePos);

        if (docRef == null) return Reference.NULL;
        else return docRef.evaluate(env);
    }

    @Accessible
    public SplElement __superclassOf__(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 1, "Class.__superclassOf__", env, lineFilePos);

        SplElement sub = arguments.getLine().get(0).evaluate(env);
        return Bool.boolValueOf(isSuperclassOf(sub, env.getMemory()));
    }

    @Override
    public SplElement getDynamicAttr(String attrName) {
        return methodPointers.get(attrName);  // nullable
    }

    @Override
    public SplElement callDynamicMethod(String methodName, Arguments arguments, Environment env, LineFilePos lineFilePos) {
        Reference methodRef = methodPointers.get(methodName);
        if (methodRef != null) {
            SplMethod method = env.getMemory().get(methodRef);
            return method.call(arguments, env);
        }
        return null;
    }

    /**
     * This method is used for gc.
     *
     * @return a list containing all pointers that should not be collected by gc.
     */
    public List<Reference> getAllAttrPointers() {
        List<Reference> res = new ArrayList<>();
        res.add(__mro__);
        res.addAll(methodPointers.values());
        return res;
    }

    @Override
    public List<Reference> listAttrReferences() {
        List<Reference> refs = new ArrayList<>();
        refs.addAll(superclassPointers);
        refs.addAll(methodPointers.values());
        refs.addAll(Arrays.asList(mroArray));
        refs.add(__mro__);
        if (classNameRef != null) refs.add(classNameRef);
        return refs;
    }

    @Override
    public String toString() {
        return "Class " + className + " ";
    }
}
