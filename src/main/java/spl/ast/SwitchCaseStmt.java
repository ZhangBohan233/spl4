package spl.ast;

import spl.interpreter.env.CaseBlockEnvironment;
import spl.interpreter.env.Environment;
import spl.util.LineFile;

import java.util.List;

public class SwitchCaseStmt extends Statement {

    private final Expression expr;
    private final List<CaseStmt> cases;
    private final CaseStmt defaultCase;

    SwitchCaseStmt(Expression expr, List<CaseStmt> cases, CaseStmt defaultCase, LineFile lineFile) {
        super(lineFile);

        this.expr = expr;
        this.cases = cases;
        this.defaultCase = defaultCase;
    }

    @Override
    protected void internalProcess(Environment env) {
        boolean execDefault = true;
        for (CaseStmt caseStmt: cases) {
            boolean caseCondition = caseStmt.evalCondition(env);
            if (caseCondition) {
                CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env, false);
                caseStmt.evaluate(blockEnv);
                if (!blockEnv.isFallingThrough()) {
                    execDefault = false;
                    break;
                }
            }
        }
        if (execDefault && defaultCase != null) {
            CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env, false);
            defaultCase.evaluate(blockEnv);
        }
    }

    @Override
    public String toString() {
        String cond = "cond{" + cases + "}";
        if (defaultCase == null) return cond;
        else {
            return cond + " default " + defaultCase;
        }
    }
}
