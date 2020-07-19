package ast;

import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;

import util.LineFile;

public abstract class Node {
    private final LineFile lineFile;

    static int spaceCount = 0;  // used for printing ast

    public Node(LineFile lineFile) {
        this.lineFile = lineFile;
    }

    public final SplElement evaluate(Environment env) {
        // pre
        if (env.interrupted()) return Pointer.NULL_PTR;

        // essential
        SplElement res = internalEval(env);

        // post

        return res;
    }

    protected abstract SplElement internalEval(Environment env);

//    protected Type inferredType(Environment env) {
//        return PrimitiveType.TYPE_VOID;
//    }

    public LineFile getLineFile() {
        return lineFile;
    }
}
