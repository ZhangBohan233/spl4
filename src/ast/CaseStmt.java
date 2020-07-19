package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class CaseStmt extends ConditionalStmt {

    final boolean isDefault;
    private Line condition;

    public CaseStmt(LineFile lineFile, boolean isDefault) {
        super(lineFile);

        this.isDefault = isDefault;
    }

    public Line getCondition() {
        return condition;
    }

    public void setCondition(Line condition) {
        this.condition = condition;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return bodyBlock.evaluate(env);
    }

    @Override
    public String toString() {
        return "case" + condition + " then " + bodyBlock;
    }
}
