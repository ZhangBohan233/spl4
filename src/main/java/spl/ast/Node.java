package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;

import spl.util.LineFile;

public abstract class Node {
    public final LineFile lineFile;

    static int spaceCount = 0;  // used for printing spl.ast

    public Node(LineFile lineFile) {
        this.lineFile = lineFile;
    }

    public final SplElement evaluate(Environment env) {
        // pre
        if (env.interrupted() || env.hasException()) return Pointer.NULL_PTR;
//        env.getMemory().enterNode(this);

        // essential
        SplElement res = internalEval(env);

        // post
//        env.getMemory().exitNode();

        return res;
    }

    protected abstract SplElement internalEval(Environment env);

    public LineFile getLineFile() {
        return lineFile;
    }

    public String reprString() {
        return toString();
    }
}
