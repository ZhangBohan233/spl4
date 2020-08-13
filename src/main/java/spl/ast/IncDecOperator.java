package spl.ast;

import spl.interpreter.splErrors.NativeError;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.SplFloat;
import spl.util.LineFile;
import spl.util.Utilities;

public class IncDecOperator extends AbstractExpression implements Buildable {

    /**
     * Is increment or not.
     *
     * If {@code true}, this node is `++`. Otherwise `--`
     */
    public final boolean isIncrement;

    /**
     * Is post increment/decrement or not.
     *
     * If {@code true}, this node is `x++` or `x--`. Otherwise, `++x` or `--x`
     */
    private boolean isPost;

    private Node value;

    public IncDecOperator(boolean isIncrement, LineFile lineFile) {
        super(lineFile);

        this.isIncrement = isIncrement;
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
//        SplElement current = vtv.getValue();
        SplElement result = null;
        if (SplElement.isPrimitive(current)) {
//            PrimitiveType pt = (PrimitiveType) vtv.getType();
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
                    Utilities.typeName(current),
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
}
