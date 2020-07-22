package ast;

import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import lexer.SyntaxError;
import util.LineFile;

public class ConditionalExpr extends BinaryExpr {

    public ConditionalExpr(String operator, LineFile lineFile) {
        super(operator, lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        if (operator.equals("_if_")) {
            if (!(right instanceof ConditionalExpr))
                throw new SyntaxError(
                        "Usage: 'true_expr if condition else false_expr'. ", getLineFile()
                );
            ConditionalExpr rightExpr = (ConditionalExpr) right;
            if (!rightExpr.operator.equals("_else_"))
                throw new SyntaxError(
                        "Usage: 'true_expr if condition else false_expr'. ", getLineFile());

            Bool cond = Bool.evalBoolean((AbstractExpression) rightExpr.left, env, getLineFile());
            if (cond.value) {
                return left.evaluate(env);
            } else {
                return rightExpr.right.evaluate(env);
            }
        } else {
            throw new SyntaxError("Unsupported ternary operator '" + operator + "'. ", getLineFile());
        }
    }
}
