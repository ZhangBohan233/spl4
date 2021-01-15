package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;

import spl.interpreter.primitives.Undefined;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.io.IOException;
import java.io.OutputStream;

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

        // essential
        SplElement res = internalEval(env);

        // post
//        if (env.hasException()) return null;

        return res;
    }

    protected abstract SplElement internalEval(Environment env);

    public final void save(BytesOut out) throws IOException {
        out.writeString(getClass().getName());
        lineFile.save(out);
        internalSave(out);
    }

    protected abstract void internalSave(BytesOut out) throws IOException;

    public LineFilePos getLineFile() {
        return lineFile;
    }

    public String reprString() {
        return toString();
    }
}
