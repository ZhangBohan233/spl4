package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import interpreter.primitives.Pointer;
import interpreter.splObjects.SplClass;
import parser.ParseError;
import util.Constants;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class ClassStmt extends Node {

    private final String className;
    private List<Node> superclassesNodes;
    //    private TypeRepresent superclass;
    private final boolean isInterface;
    private final boolean isAbstract;
    //    private TemplateNode templateNode;
    private BlockStmt body;

    public ClassStmt(String className, boolean isInterface, boolean isAbstract, LineFile lineFile) {
        super(lineFile);

        this.className = className;
        this.isInterface = isInterface;
        this.isAbstract = isAbstract;
    }

    public void setBody(BlockStmt body) {
        this.body = body;
    }

//    public void setImplements(Implements implementations) {
//        this.implementations = implementations;
//    }
//
//    public void setTemplateNode(TemplateNode templateNode) {
//        this.templateNode = templateNode;
//    }

    public void setSuperclasses(Line extensions) {
        this.superclassesNodes = extensions.getChildren();
//        if (extendNode instanceof Extends) {
//            superclass = ((Extends) extendNode).getValue();
//        } else {
//            throw new ParseError("Superclass must be a class. ", getLineFile());
//        }
    }

    private void validateExtending() {
        if (superclassesNodes == null) {
            if (!className.equals(Constants.OBJECT_CLASS)) {
                superclassesNodes = new ArrayList<>();
                superclassesNodes.add(new NameNode("Object", getLineFile()));
            }
        }
    }

    @Override
    protected SplElement internalEval(Environment env) {

        validateExtending();

//        System.out.println(superclass);

//        ClassType superclassT;
//        if (superclass == null) {
//            superclassT = null;
//        } else {
//            superclassT = (ClassType) superclass.evalType(env);
//        }

        List<Pointer> superclassesPointers = new ArrayList<>();
        for (int i = superclassesNodes.size() - 1; i >= 0; i--) {
            Pointer scPtr = (Pointer) superclassesNodes.get(i).evaluate(env);
            superclassesPointers.add(scPtr);
        }

//        List<ClassType> interfacePointers = new ArrayList<>();
//        for (Node node : implementations.getExtending().getChildren()) {
//            if (node instanceof TypeRepresent) {
//                ClassType t = (ClassType) ((TypeRepresent) node).evalType(env);
//                interfacePointers.add(t);
//            } else {
//                throw new SplException();
//            }
//        }

        // TODO: check implementations

//        List<Node> templateList;
//        if (templateNode == null) {
//            templateList = new ArrayList<>();
//        } else {
//            templateList = templateNode.value.getChildren();
//        }

        SplClass clazz = new SplClass(className, superclassesPointers, body, env, isAbstract, isInterface);
        Pointer clazzPtr = env.getMemory().allocate(1, env);
        env.getMemory().set(clazzPtr, clazz);
//        ClassType clazzType = new ClassType(clazzPtr);

        env.defineVar(className, getLineFile());
//        TypeValue typeValue = new TypeValue(clazzType, clazzPtr);

        env.setVar(className, clazzPtr, getLineFile());

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
