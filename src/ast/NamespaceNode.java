package ast;

import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.SplModule;
import util.LineFile;

public class NamespaceNode extends UnaryStmt {

    public NamespaceNode(String name, LineFile lineFile) {
        super("namespace", true, lineFile);

        value = new NameNode(name, lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {

        SplElement moduleTv = value.evaluate(env);
        SplModule module = (SplModule) env.getMemory().get((Pointer) moduleTv);
        env.addNamespace(module.getEnv());
    }

    @Override
    public String toString() {
        return "namespace " + value;
    }
}
