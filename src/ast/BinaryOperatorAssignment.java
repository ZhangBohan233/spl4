package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class BinaryOperatorAssignment extends BinaryExpr {

    private final String realOp;
    private final int type;

    public BinaryOperatorAssignment(String operator, int type, LineFile lineFile) {
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
