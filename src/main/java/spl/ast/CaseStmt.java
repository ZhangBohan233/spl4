package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;

public class CaseStmt extends Statement {

    public final Node bodyBlock;
    public final boolean isExpr;
    private final Line conditions;  // in 'default', it is null
    private final BinaryOperator[] binaryConditions;

    public CaseStmt(Line conditions, Node bodyBlock, boolean isExpr, LineFilePos lineFile) {
        super(lineFile);

        this.conditions = conditions;
        this.bodyBlock = bodyBlock;
        this.isExpr = isExpr;

        if (conditions == null) {
            binaryConditions = null;
        } else {
            binaryConditions = new BinaryOperator[conditions.size()];
            for (int i = 0; i < binaryConditions.length; i++) {
                BinaryOperator binaryCondition = new BinaryOperator("==", BinaryOperator.LOGICAL, lineFile);
                binaryCondition.right = (Expression) conditions.get(i);
                binaryConditions[i] = binaryCondition;
            }
        }
    }

    public static CaseStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        Node body = Reconstructor.reconstruct(is);
        boolean expr = is.readBoolean();
        boolean hasCond = is.readBoolean();
        Line cond = null;
        if (hasCond) cond = Reconstructor.reconstruct(is);

        return new CaseStmt(cond, body, expr, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        bodyBlock.save(out);
        out.writeBoolean(isExpr);
        out.writeBoolean(conditions != null);
        if (conditions != null)
            conditions.save(out);
        // do not save binary condition
    }

    public void setSwitchExpr(Expression switchExpr) {
//        binaryCondition.left = switchExpr;
        if (binaryConditions != null)
            for (BinaryOperator bo : binaryConditions) {
                bo.left = switchExpr;
            }
    }

    public boolean evalCondition(Environment env) {
//        return Bool.evalBoolean(binaryCondition, env, lineFile).value;
        if (binaryConditions == null) return true;
        for (BinaryOperator bo : binaryConditions) {
            Bool val = Bool.evalBoolean(bo, env, lineFile);
            if (val.value) return true;
        }
        return false;
    }

    public boolean isDefault() {
        return conditions == null;
    }

    @Override
    protected void internalProcess(Environment env) {
        bodyBlock.evaluate(env);
    }

    @Override
    public String toString() {
        String arrow = isExpr ? " -> " : " ";
        return isDefault() ? ("default: " + arrow + bodyBlock) : ("case " + conditions + arrow + bodyBlock);
    }
}
