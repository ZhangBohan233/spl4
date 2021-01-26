package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.util.BytesOut;
import spl.util.LineFilePos;

import java.io.IOException;

public abstract class Node {
    static int spaceCount = 0;  // used for printing spl.ast
    protected LineFilePos lineFile;

    public Node(LineFilePos lineFile) {
        this.lineFile = lineFile;
    }

    public final SplElement evaluate(Environment env) {
        // pre
        env.getMemory().waitGc();
        if (env.interrupted()) return Reference.NULL;
        if (env.hasException()) return Undefined.ERROR;

        // essential
        SplElement res = internalEval(env);

        // post
//        if (env.hasException()) return Undefined.ERROR;

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
