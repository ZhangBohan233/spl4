package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.util.Constants;
import spl.util.LineFile;

public class ArrayLiteral extends Expression {

    private final Arguments content;

    public ArrayLiteral(Arguments content, LineFile lineFile) {
        super(lineFile);

        this.content = content;
    }

    @Override
    public String toString() {
        return "list[" + content + "]";
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return Instance.createInstanceWithInitCall(Constants.LIST_CLASS, content.evalArgs(env), env, lineFile).pointer;
    }
}
