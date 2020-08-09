package interpreter.splObjects;

import ast.*;
import interpreter.splErrors.AttributeError;
import interpreter.Memory;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splErrors.RuntimeSyntaxError;
import util.Constants;
import util.LineFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplClass extends SplObject {

    /**
     * Pointer to superclasses, ordered from closet to furthest.
     * <p>
     * For instance, a pointer to class 'Object' is always the last element in this list
     */
    private final List<Pointer> superclassPointers;

//    private final BlockStmt body;
    private final String className;
    private final Environment definitionEnv;

    private final List<Node> classNodes = new ArrayList<>();
    private final Map<String, Pointer> methodPointers = new HashMap<>();

    public SplClass(String className, List<Pointer> superclassPointers,
                    BlockStmt body, Environment definitionEnv) {
        this.className = className;
        this.superclassPointers = superclassPointers;
//        this.body = body;
        this.definitionEnv = definitionEnv;

        evalBody(body);
        checkConstructor();
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
//                    lineNode.evaluate(instanceEnv);
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

    public List<Pointer> getSuperclassPointers() {
        return superclassPointers;
    }

//    public BlockStmt getBody() {
//        return body;
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
                List<Pointer> mroList = new ArrayList<>();
                addToMroList(mroList, env.getMemory(), selfPtr);
                return mroListToArray(mroList, lineFile);
            }
        }
        throw new AttributeError("Class does not have attribute '" + attrNode + "'. ", lineFile);
    }

    @Override
    public String toString() {
        return "Class <" + className + ">";
    }

    private void addToMroList(List<Pointer> mro, Memory memory, Pointer selfPtr) {
        mro.add(selfPtr);
        for (Pointer scPtr: superclassPointers) {
            SplClass sc = (SplClass) memory.get(scPtr);
            sc.addToMroList(mro, memory, scPtr);
        }
    }

    private void removeDuplicateMro(List<Pointer> mro) {

    }

    private Pointer mroListToArray(List<Pointer> mro, LineFile lineFile) {
        Pointer mroArrPtr = SplArray.createArray(SplElement.POINTER, mro.size(), definitionEnv);
        for (int i = 0; i < mro.size(); i++) {
            SplArray.setItemAtIndex(mroArrPtr, i, mro.get(i), definitionEnv, lineFile);
        }
        return mroArrPtr;
    }
}
