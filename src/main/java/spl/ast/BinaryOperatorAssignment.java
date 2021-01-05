package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class BinaryOperatorAssignment extends BinaryExpr {

    private final String realOp;
    private final int type;

    public BinaryOperatorAssignment(String operator, int type, LineFilePos lineFile) {
        super(operator, lineFile);

        realOp = operator.substring(0, operator.length() - 1);
        this.type = type;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        BinaryOperator binaryOperator = new BinaryOperator(realOp, type, getLineFile());
        binaryOperator.setLeft(left);
        binaryOperator.setRight(right);
        Assignment assignment = new Assignment(getLineFile());
        assignment.setLeft(left);
        assignment.setRight(binaryOperator);
        return assignment.evaluate(env);
    }

}
