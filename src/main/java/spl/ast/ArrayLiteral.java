package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Instance;
import spl.util.*;

import java.io.IOException;

public class ArrayLiteral extends Expression {

    private final Arguments content;

    public ArrayLiteral(Arguments content, LineFilePos lineFile) {
        super(lineFile);

        this.content = content;
    }

    public static ArrayLiteral reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        Arguments arguments = Reconstructor.reconstruct(is);
        return new ArrayLiteral(arguments, lineFilePos);
    }

    @Override
    public String toString() {
        return "list[" + content + "]";
    }

    @Override
    protected SplElement internalEval(Environment env) {
        var ea = content.evalArgs(env);
        if (env.hasException()) return Undefined.ERROR;
        Instance.InstanceAndPtr iap = Instance.createInstanceWithInitCall(Constants.LIST_CLASS, ea, env, lineFile);
        if (iap == null) return Undefined.ERROR;
        return iap.pointer;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        content.save(out);
    }
}
