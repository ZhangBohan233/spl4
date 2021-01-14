package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ArrayLiteral extends Expression {

    private final Arguments content;

    public ArrayLiteral(Arguments content, LineFilePos lineFile) {
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

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        content.save(out);
    }

    public static ArrayLiteral reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        Arguments arguments = Reconstructor.reconstruct(is);
        return new ArrayLiteral(arguments, lineFilePos);
    }
}
