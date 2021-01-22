package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class GenericNode extends Expression {

    private final Node obj;
    private final Line genericLine;

    public GenericNode(Node obj, Line genericLine, LineFilePos lineFile) {
        super(lineFile);

        this.obj = obj;
        this.genericLine = genericLine;
    }

    public static GenericNode reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Expression callObj = Reconstructor.reconstruct(in);
        Line generics = Reconstructor.reconstruct(in);
        return new GenericNode(callObj, generics, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        obj.save(out);
        genericLine.save(out);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return obj.evaluate(env);
    }

    public Line getGenericLine() {
        return genericLine;
    }

    public Node getObj() {
        return obj;
    }

    @Override
    public String toString() {
        return "GenericNode{" + obj + "<" + genericLine + ">}";
    }
}
