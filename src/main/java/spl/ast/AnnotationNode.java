package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.interpreter.splObjects.SplClass;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.io.IOException;
import java.util.List;

public class AnnotationNode extends NonEvaluate {

    private final String annotation;

    public AnnotationNode(String annotation, LineFilePos lineFile) {
        super(lineFile);

        this.annotation = annotation;
    }

    public static Reference evalAnnotations(List<AnnotationNode> annotationNodes,
                                              Environment env,
                                              LineFilePos lineFilePos) {
        int size;
        if (annotationNodes == null) size = 0;
        else size = annotationNodes.size();

        SplClass annClass = null;

        Reference arrayRef = SplArray.createArray(SplElement.POINTER, size, env, lineFilePos);
        if (env.hasException()) return null;
        for (int i = 0; i < size; i++) {
            AnnotationNode an = annotationNodes.get(i);
            Instance.InstanceAndPtr iap = Instance.createInstanceAndAllocate(an.annotation, env, lineFilePos);
            if (iap == null) {
                return null;
            }
            // do not set 'annClass' before loop, since the class 'Annotation' might not be evaluated.
            // Any class before 'Annotation' must not have annotations
            if (annClass == null) annClass = env.getMemory().get((Reference) env.get(Constants.ANNOTATION, lineFilePos));
            if (!annClass.isInstance(iap.pointer, env.getMemory())) {
                SplInvokes.throwException(
                        env,
                        Constants.TYPE_ERROR,
                        "Annotations must extends 'Annotation'.",
                        lineFilePos
                );
                return null;
            }
            SplArray.setItemAtIndex(arrayRef, i, iap.pointer, env, lineFilePos);
        }
        return arrayRef;
    }

    public static AnnotationNode reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String s = is.readString();
        return new AnnotationNode(s, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(annotation);
    }

    @Override
    public String toString() {
        return "@" + annotation;
    }
}
