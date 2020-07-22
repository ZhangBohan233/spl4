package ast;

import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.Int;
import interpreter.primitives.SplElement;
import interpreter.primitives.SplFloat;
import lexer.SyntaxError;
import util.LineFile;

public class RegularUnaryOperator extends UnaryExpr {

    public static final int NUMERIC = 1;
    public static final int LOGICAL = 2;
    private final int type;

    public RegularUnaryOperator(String op, int type, LineFile lineFile) {
        super(op, true, lineFile);

        this.type = type;
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
        throw new SyntaxError("Operator error. ", getLineFile());
    }

}
