package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.SplModule;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class NamespaceNode extends UnaryStmt {

    public NamespaceNode(String name, LineFilePos lineFile) {
        super("namespace", true, lineFile);

        value = new NameNode(name, lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {

        SplElement moduleTv = value.evaluate(env);
        SplModule module = (SplModule) env.getMemory().get((Reference) moduleTv);
        env.addNamespace(module.getEnv());
    }

    @Override
    public String toString() {
        return "namespace " + value;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(((NameNode) value).getName());
    }

    public static NamespaceNode reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        String name = in.readString();
        return new NamespaceNode(name, lineFilePos);
    }
}
