package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class CaseStmt extends AbstractStatement {

    public final Node bodyBlock;
    private final AbstractExpression condition;
    public final boolean isExpr;

    public CaseStmt(AbstractExpression condition, Node bodyBlock, boolean isExpr, LineFile lineFile) {
        super(lineFile);

        this.condition = condition;
        this.bodyBlock = bodyBlock;
        this.isExpr = isExpr;
    }

    public AbstractExpression getCondition() {
        return condition;
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
