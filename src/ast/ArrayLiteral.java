package ast;

import interpreter.Memory;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Instance;
import util.Constants;
import util.LineFile;

import java.util.List;

public class ArrayLiteral extends AbstractExpression {

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
        Instance.InstanceAndPtr iap = Instance.createInstanceAndAllocate(Constants.LIST_CLASS, env, getLineFile());
        Instance.callInit(iap, content, env, getLineFile());

        return iap.pointer;
    }
}
