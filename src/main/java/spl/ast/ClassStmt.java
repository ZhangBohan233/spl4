package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.CheckerFunction;
import spl.interpreter.splObjects.SplClass;
import spl.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassStmt extends Expression {

    private final String className;
    private final BlockStmt body;
    private final StringLiteralRef docRef;
    private final List<Node> templates;  // nullable
    private List<Node> superclassesNodes;  // nullable
    private final boolean isConst;

    /**
     * @param className  name of class
     * @param extensions extending node list, null if not specified.
     * @param templates  template node list, null if not specified
     * @param body       body block
     * @param docRef     string literal reference of docstring
     * @param lineFile   line file
     */
    public ClassStmt(String className,
                     List<Node> extensions,
                     List<Node> templates,
                     BlockStmt body,
                     StringLiteralRef docRef,
                     boolean isConst,
                     LineFilePos lineFile) {
        super(lineFile);

        this.className = className;
        this.superclassesNodes = extensions;
        this.templates = templates;
        this.body = body;
        this.docRef = docRef;
        this.isConst = isConst;
    }

    public static ClassStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String name = is.readString();
        BlockStmt body = Reconstructor.reconstruct(is);
        boolean isConst = is.readBoolean();
        boolean hasSc = is.readBoolean();
        List<Node> superclassNodes = null;
        if (hasSc) superclassNodes = is.readList();
        boolean hasTemplates = is.readBoolean();
        List<Node> templates = null;
        if (hasTemplates) templates = is.readList();
        boolean hasDoc = is.readBoolean();
        StringLiteralRef docRef = null;
        if (hasDoc) docRef = Reconstructor.reconstruct(is);
        return new ClassStmt(name, superclassNodes, templates, body, docRef, isConst, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(className);
        body.save(out);
        out.writeBoolean(isConst);
        out.writeBoolean(superclassesNodes != null);
        if (superclassesNodes != null) out.writeList(superclassesNodes);
        out.writeBoolean(templates != null);
        if (templates != null) out.writeList(templates);
        out.writeBoolean(docRef != null);
        if (docRef != null) docRef.save(out);
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
    protected SplElement internalEval(Environment env) { ;
        return crossEnvEval(env, env);
    }

    public SplElement crossEnvEval(Environment supClassDefEnv, Environment callingEnv) {
        validateExtending();

        List<Reference> superclassesPointers = new ArrayList<>();
        Map<Reference, Line> superclassGenerics = new HashMap<>();
        for (Node superclassesNode : superclassesNodes) {
            Reference scPtr = (Reference) superclassesNode.evaluate(supClassDefEnv);
            superclassesPointers.add(scPtr);
            if (superclassesNode instanceof GenericNode) {
                superclassGenerics.put(scPtr, ((GenericNode) superclassesNode).getGenericLine());
            }
        }
        if (superclassGenerics.size() == 0) superclassGenerics = null;

        String[] templates = null;
        if (this.templates != null) {
            templates = ContractNode.getDefinedTemplates(this.templates, callingEnv, lineFile);
            if (callingEnv.hasException()) return Undefined.ERROR;
        }

        SplElement clazzPtr =
                SplClass.createClassAndAllocate(className, superclassesPointers, templates, superclassGenerics,
                        body, callingEnv, docRef, isConst, lineFile);
        if (clazzPtr == Undefined.ERROR) return Undefined.ERROR;  // a quicker way to check env.hasException()

        callingEnv.defineVarAndSet(className, clazzPtr, getLineFile());

        String iofName = className + "?";
        CheckerFunction instanceOfFunc = new CheckerFunction(iofName, (Reference) clazzPtr);
        Reference iofPtr = callingEnv.getMemory().allocateFunction(instanceOfFunc, callingEnv, callingEnv.getThreadId());
        callingEnv.defineVarAndSet(iofName, iofPtr, getLineFile());

        SplClass clazz = callingEnv.getMemory().get((Reference) clazzPtr);
        clazz.setChecker(iofPtr);

        return clazzPtr;
    }

    @Override
    public String toString() {
        return String.format("class %s(%s) %s", className, superclassesNodes, body);
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

    public String getClassName() {
        return className;
    }

    public List<Node> getTemplates() {
        return templates;
    }
}
