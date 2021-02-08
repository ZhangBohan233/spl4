package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.SplCallable;
import spl.interpreter.splObjects.SplMethod;
import spl.util.*;

import java.io.IOException;
import java.util.List;

public class FuncDefinition extends Expression {

    public final NameNode name;
    private final Line parameters;
    private final BlockStmt body;
    private final Line templateLine;  // nullable
    private final StringLiteralRef docRef;  // nullable
    private final List<AnnotationNode> annotations;  // nullable
    private final boolean isConst;

    public FuncDefinition(NameNode name,
                          Line parameters,
                          BlockStmt body,
                          Line templateLine,
                          StringLiteralRef docRef,
                          List<AnnotationNode> annotations,
                          boolean isConst,
                          LineFilePos lineFile) {
        super(lineFile);

        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.templateLine = templateLine;
        this.docRef = docRef;
        this.annotations = annotations;
        this.isConst = isConst;
    }

    public FuncDefinition(NameNode name,
                          Line parameters,
                          BlockStmt body,
                          LineFilePos lineFile) {
        this(
                name,
                parameters,
                body,
                null,
                null,
                null,
                false,
                lineFile);
    }

    public static FuncDefinition reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        NameNode name = Reconstructor.reconstruct(is);
        Line params = Reconstructor.reconstruct(is);
        BlockStmt body = Reconstructor.reconstruct(is);
        boolean isConst = is.readBoolean();
        boolean hasTemplate = is.readBoolean();
        Line templateLine = null;
        if (hasTemplate) templateLine = Reconstructor.reconstruct(is);
        boolean hasDoc = is.readBoolean();
        StringLiteralRef docRef = null;
        if (hasDoc) docRef = Reconstructor.reconstruct(is);
        List<AnnotationNode> ann = is.readOptionalList();
        return new FuncDefinition(name, params, body, templateLine, docRef, ann, isConst, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        name.save(out);
        parameters.save(out);
        body.save(out);
        out.writeBoolean(isConst);
        out.writeBoolean(templateLine != null);
        if (templateLine != null) templateLine.save(out);
        out.writeBoolean(docRef != null);
        if (docRef != null) docRef.save(out);
        out.writeOptional(annotations);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        Function.Parameter[] params = SplCallable.evalParams(parameters, env);
        if (env.hasException()) return Undefined.ERROR;
        Reference annotationArrRef = AnnotationNode.evalAnnotations(annotations, env, lineFile);
        if (annotationArrRef == null) return Undefined.ERROR;

        Function function = new Function(body, params, env, name.getName(), docRef, annotationArrRef, getLineFile());
        Reference funcPtr = env.getMemory().allocateFunction(function, env);

        if (isConst) {
            env.defineConstFunction(name.getName(), funcPtr, getLineFile());
        } else {
            env.defineFunction(name.getName(), funcPtr, getLineFile());
        }
        return funcPtr;
    }

    public SplElement evalAsMethod(Environment classDefEnv, int defClassId) {
        Function.Parameter[] oldParams = SplCallable.evalParams(parameters, classDefEnv);
        if (classDefEnv.hasException()) return Undefined.ERROR;
        Reference annotationArrRef = AnnotationNode.evalAnnotations(annotations, classDefEnv, lineFile);
        if (annotationArrRef == null) return Undefined.ERROR;

        assert oldParams != null;
        Function.Parameter[] params = SplCallable.insertThis(oldParams);

        SplMethod function = new SplMethod(body, params, classDefEnv, name.getName(), docRef, annotationArrRef,
                defClassId, getLineFile());

        return classDefEnv.getMemory().allocateFunction(function, classDefEnv);
    }

    @Override
    public String toString() {
        String prefix = annotations == null ? "" : (annotations.toString() + " ");

        if (name == null)
            return String.format("%sfn(%s): %s", prefix, parameters, body);
        else
            return String.format("%sfn %s(%s): %s", prefix, name, parameters, body);
    }

    @Override
    public String reprString() {
        return "fn " + name;
    }

    public Line getParameters() {
        return parameters;
    }

    public BlockStmt getBody() {
        return body;
    }

    public NameNode getName() {
        return name;
    }

    public boolean isConst() {
        return isConst;
    }
}
