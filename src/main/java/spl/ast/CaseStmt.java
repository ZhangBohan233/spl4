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
    private final Expression condition;
    private final BinaryOperator binaryCondition;

    public CaseStmt(Expression condition, Node bodyBlock, boolean isExpr, LineFilePos lineFile) {
        super(lineFile);

        this.condition = condition;
        this.bodyBlock = bodyBlock;
        this.isExpr = isExpr;

        binaryCondition = new BinaryOperator("==", BinaryOperator.LOGICAL, lineFile);
        binaryCondition.right = condition;
    }

    public static CaseStmt reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        Node body = Reconstructor.reconstruct(is);
        boolean expr = is.readBoolean();
        boolean hasCond = is.readBoolean();
        Expression cond = null;
        if (hasCond) cond = Reconstructor.reconstruct(is);

        return new CaseStmt(cond, body, expr, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        bodyBlock.save(out);
        out.writeBoolean(isExpr);
        out.writeBoolean(condition != null);
        if (condition != null)
            condition.save(out);
        // do not save binary condition
    }

    public void setSwitchExpr(Expression switchExpr) {
        binaryCondition.left = switchExpr;
    }

    public boolean evalCondition(Environment env) {
        return Bool.evalBoolean(binaryCondition, env, lineFile).value;
    }

    public boolean isDefault() {
        return condition == null;
    }

    @Override
    protected void internalProcess(Environment env) {
        bodyBlock.evaluate(env);
    }

    @Override
    public String toString() {
        String arrow = isExpr ? " -> " : " ";
        return isDefault() ? ("default: " + arrow + bodyBlock) : ("case " + condition + arrow + bodyBlock);
    }
}
