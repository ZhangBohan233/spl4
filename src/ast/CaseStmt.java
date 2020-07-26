package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class CaseStmt extends ConditionalStmt {

    private final AbstractExpression condition;

    public CaseStmt(AbstractExpression condition, BlockStmt bodyBlock, LineFile lineFile) {
        super(lineFile);

        this.condition = condition;
        this.bodyBlock = bodyBlock;
    }

    public AbstractExpression getCondition() {
        return condition;
    }

    public boolean isDefault() {
        return condition == null;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return bodyBlock.evaluate(env);
    }

    @Override
    public String toString() {
        return isDefault() ? ("default: " + bodyBlock) : ("case " + condition + " then " + bodyBlock);
    }
}
