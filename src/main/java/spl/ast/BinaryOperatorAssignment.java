package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.*;

import java.io.IOException;

public class BinaryOperatorAssignment extends BinaryExpr {

    private final int type;
    private final Assignment assignment;  // do not save this

    public BinaryOperatorAssignment(String operator, int type, LineFilePos lineFile) {
        super(operator, lineFile);

        String realOp = operator.substring(0, operator.length() - 1);
        this.type = type;

        BinaryOperator binaryOperator = new BinaryOperator(realOp, type, getLineFile());
        binaryOperator.setLeft(left);
        binaryOperator.setRight(right);
        assignment = new Assignment(getLineFile());
        assignment.setLeft(left);
        assignment.setRight(binaryOperator);
    }

    public static BinaryOperatorAssignment reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        String op = is.readString();
        Expression left = Reconstructor.reconstruct(is);
        Expression right = Reconstructor.reconstruct(is);

        int type = is.readInt();
        BinaryOperatorAssignment boa = new BinaryOperatorAssignment(op, type, lineFilePos);
        boa.setLeft(left);
        boa.setRight(right);
        return boa;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return assignment.evaluate(env);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        super.internalSave(out);

        out.write(Utilities.intToBytes(type));
    }
}
