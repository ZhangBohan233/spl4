package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        super.internalSave(out);

        out.write(Utilities.intToBytes(type));
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
}
