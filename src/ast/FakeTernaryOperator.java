package ast;

import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import lexer.SyntaxError;
import util.LineFile;

public class FakeTernaryOperator extends BinaryExpr {

    public FakeTernaryOperator(String operator, LineFile lineFile) {
        super(operator, lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (operator.equals("?")) {
            if (!(right instanceof Declaration)) throw new SyntaxError(
                    "Usage: 'expr ? if true : if false'. ", getLineFile()
            );
            return null;
//            Declaration rd = (Declaration) right;
//            Bool bool = Bool.evalBoolean(left, env, getLineFile());
//            if (bool.value) {
//                return rd.left.evaluate(env);
//            } else {
//                return rd.right.evaluate(env);
//            }

        } else {
            throw new SyntaxError("Unsupported ternary operator '" + operator + "'. ", getLineFile());
        }
    }
}
