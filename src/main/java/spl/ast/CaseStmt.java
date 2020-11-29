package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.util.LineFile;

public class CaseStmt extends Statement {

    public final Node bodyBlock;
    private final Expression condition;
    private final BinaryOperator binaryCondition;
    public final boolean isExpr;

    public CaseStmt(Expression condition, Node bodyBlock, boolean isExpr, LineFile lineFile) {
        super(lineFile);

        this.condition = condition;
        this.bodyBlock = bodyBlock;
        this.isExpr = isExpr;

        binaryCondition = new BinaryOperator("==", BinaryOperator.LOGICAL, lineFile);
        binaryCondition.right = condition;
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
