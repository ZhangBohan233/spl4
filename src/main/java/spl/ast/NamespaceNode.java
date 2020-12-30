package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.SplModule;
import spl.util.LineFile;

public class NamespaceNode extends UnaryStmt {

    public NamespaceNode(String name, LineFile lineFile) {
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
}
