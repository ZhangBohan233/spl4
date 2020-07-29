package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class NameNode extends AbstractExpression {
    private final String name;

    public NameNode(String name, LineFile lineFile) {
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
//        System.out.println(env.get(name, getLineFile()) + name);
        return env.get(name, getLineFile());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NameNode && ((NameNode) obj).name.equals(name);
    }
}
