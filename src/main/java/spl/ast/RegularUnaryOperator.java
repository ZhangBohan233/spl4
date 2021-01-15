package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.SplFloat;
import spl.util.*;

import java.io.IOException;

public class RegularUnaryOperator extends UnaryExpr {

    public static final int NUMERIC = 1;
    public static final int LOGICAL = 2;
    private final int type;

    public RegularUnaryOperator(String op, int type, LineFilePos lineFile) {
        super(op, true, lineFile);

        this.type = type;
    }

    public static RegularUnaryOperator reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        String op = in.readString();  // op
        int type = in.readInt();
        Expression value = Reconstructor.reconstruct(in);
        var ruo = new RegularUnaryOperator(op, type, lineFilePos);
        ruo.setValue(value);
        return ruo;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(operator);
        out.writeInt(type);
        value.save(out);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement valueTv = value.evaluate(env);
        if (type == NUMERIC) {
            if (valueTv.isIntLike()) {
                if (operator.equals("neg")) {
                    return new Int(-valueTv.intValue());
                }
            } else if (valueTv instanceof SplFloat) {
                if (operator.equals("neg")) {
                    return new SplFloat(-valueTv.floatValue());
                }
            }
        } else if (type == LOGICAL) {
            if (valueTv instanceof Bool) {
                if (operator.equals("not")) {
                    return Bool.boolValueOf(!((Bool) valueTv).value);
                }
            }
        }
        return SplInvokes.throwExceptionWithError(
                env,
                Constants.TYPE_ERROR,
                "Operator error ",
                lineFile);
    }
}
