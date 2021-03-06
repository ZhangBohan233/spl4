package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.SplFloat;
import spl.interpreter.splErrors.NativeError;
import spl.util.*;

import java.io.IOException;

public class IncDecOperator extends Expression implements Buildable {

    /**
     * Is increment or not.
     * <p>
     * If {@code true}, this node is `++`. Otherwise `--`
     */
    public final boolean isIncrement;

    /**
     * Is post increment/decrement or not.
     * <p>
     * If {@code true}, this node is `x++` or `x--`. Otherwise, `++x` or `--x`
     */
    private boolean isPost;

    private Node value;

    public IncDecOperator(boolean isIncrement, LineFilePos lineFile) {
        super(lineFile);

        this.isIncrement = isIncrement;
    }

    public static IncDecOperator reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Node value = Reconstructor.reconstruct(in);
        boolean isInc = in.readBoolean();
        boolean isPost = in.readBoolean();
        var ido = new IncDecOperator(isInc, lineFilePos);
        ido.setValue(value);
        ido.setPost(isPost);
        return ido;
    }

    public void setPost(boolean post) {
        isPost = post;
    }

    public void setValue(Node value) {
        this.value = value;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement current = value.evaluate(env);
        SplElement result = null;
        if (SplElement.isPrimitive(current)) {
            if (current.isIntLike()) {
                if (isIncrement) {
                    result = new Int(current.intValue() + 1);
                } else {
                    result = new Int(current.intValue() - 1);
                }
            } else if (current instanceof SplFloat) {
                if (isIncrement) {
                    result = new SplFloat(current.floatValue() + 1);
                } else {
                    result = new SplFloat(current.floatValue() - 1);
                }
            }
        }
        if (result == null) {
            throw new NativeError("Increment/decrement operator is not applicable to type " +
                    Utilities.typeName(current, env, lineFile) + ". ",
                    getLineFile());
        }

        Assignment.assignment(value, result, env, getLineFile());
        if (isPost) {
            return current;
        } else {
            return result;
        }
    }

    @Override
    public boolean notFulfilled() {
        return value == null;
    }

    @Override
    public String toString() {
        if (isIncrement) {
            return isPost ? value + "++" : "++" + value;
        } else {
            return isPost ? value + "--" : "--" + value;
        }
    }

    @Override
    public String getOperator() {
        return isIncrement ? "++" : "--";
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        value.save(out);
        out.writeBoolean(isIncrement);
        out.writeBoolean(isPost);
    }
}
