package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.NativeFunction;
import spl.interpreter.splObjects.SplClass;
import spl.interpreter.splObjects.SplObject;
import spl.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassStmt extends Expression {

    private final String className;
    private final BlockStmt body;
    private final StringLiteralRef docRef;
    private List<Node> superclassesNodes;  // nullable
    private List<Node> templates;  // nullable

    /**
     * @param className  name of class
     * @param extensions extending node list, null if not specified.
     * @param templates  template node list, null if not specified
     * @param body       body block
     * @param docRef     string literal reference of docstring
     * @param lineFile   line file
     */
    public ClassStmt(String className, List<Node> extensions, List<Node> templates, BlockStmt body, StringLiteralRef docRef,
                     LineFilePos lineFile) {
        super(lineFile);

        this.className = className;
        this.superclassesNodes = extensions;
        this.templates = templates;
        this.body = body;
        this.docRef = docRef;
    }

    public static ClassStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String name = is.readString();
        BlockStmt body = Reconstructor.reconstruct(is);
        boolean hasSc = is.readBoolean();
        List<Node> superclassNodes = null;
        if (hasSc) superclassNodes = is.readList();
        boolean hasTemplates = is.readBoolean();
        List<Node> templates = null;
        if (hasTemplates) templates = is.readList();
        boolean hasDoc = is.readBoolean();
        StringLiteralRef docRef = null;
        if (hasDoc) docRef = Reconstructor.reconstruct(is);
        return new ClassStmt(name, superclassNodes, templates, body, docRef, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(className);
        body.save(out);
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
    protected SplElement internalEval(Environment env) {
        validateExtending();

        List<Reference> superclassesPointers = new ArrayList<>();
        for (Node superclassesNode : superclassesNodes) {
            Reference scPtr = (Reference) superclassesNode.evaluate(env);
            superclassesPointers.add(scPtr);
        }

        String[] templates = null;
        if (this.templates != null) {
            templates = ContractNode.getDefinedTemplates(this.templates, env, lineFile);
            if (env.hasException()) return Undefined.ERROR;
        }

        SplElement clazzPtr =
                SplClass.createClassAndAllocate(className, superclassesPointers, templates, body, env, docRef,
                        lineFile);
        if (clazzPtr == Undefined.ERROR) return Undefined.ERROR;  // a quicker way to check env.hasException()

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
                        return Bool.boolValueOf(SplClass.isSuperclassOf(
                                (Reference) clazzPtr,
                                argClazzPtr,
                                callingEnv.getMemory()));
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
