package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;

import spl.interpreter.primitives.Undefined;
import spl.util.LineFilePos;

public abstract class Node {
    public final LineFilePos lineFile;

    static int spaceCount = 0;  // used for printing spl.ast

    public Node(LineFilePos lineFile) {
        this.lineFile = lineFile;
    }

    public final SplElement evaluate(Environment env) {
        // pre
        if (env.interrupted()) return Reference.NULL;
        if (env.hasException()) return Undefined.ERROR;
//        env.getMemory().enterNode(this);

        // essential
        SplElement res = internalEval(env);

        // post
//        env.getMemory().exitNode();
//        if (env.hasException()) return null;

        return res;
    }

    protected abstract SplElement internalEval(Environment env);

    public LineFilePos getLineFile() {
        return lineFile;
    }

    public String reprString() {
        return toString();
    }
}
