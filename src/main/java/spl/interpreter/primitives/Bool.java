package spl.interpreter.primitives;

import spl.ast.AbstractExpression;
import spl.interpreter.env.Environment;
import spl.interpreter.splErrors.TypeError;
import spl.util.LineFile;

public class Bool extends SplElement {

    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);

    public final boolean value;

    private Bool(boolean value) {
        this.value = value;
    }

    @Override
    public boolean isIntLike() {
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int type() {
        return SplElement.BOOLEAN;
    }

    @Override
    public long intValue() {
        return value ? 1 : 0;
    }

    @Override
    public double floatValue() {
        return intValue();
    }

    @Override
    public boolean booleanValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bool anInt = (Bool) o;

        return value == anInt.value;
    }

    public static Bool boolValueOf(boolean b) {
        return b ? Bool.TRUE : Bool.FALSE;
    }

    public static Bool evalBoolean(AbstractExpression node, Environment env, LineFile lineFile) {
        SplElement cond = node.evaluate(env);
        if (cond instanceof Bool)
            return (Bool) cond;
        else
            throw new TypeError("Statement takes " +
                    "boolean value as condition. ", lineFile);
    }
}
