package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NameNode extends Expression {
    private final String name;

    public NameNode(String name, LineFilePos lineFile) {
        super(lineFile);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Name(" + name + ")";
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return env.get(name, getLineFile());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NameNode && ((NameNode) obj).name.equals(name);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(name);
    }

    public static NameNode reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        return new NameNode(is.readString(), lineFilePos);
    }
}
