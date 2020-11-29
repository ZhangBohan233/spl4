package spl.ast;

import spl.interpreter.env.CaseBlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

import java.util.List;

public class SwitchCaseExpr extends Expression {

    private final Expression expr;
    private final List<CaseStmt> cases;
    private final CaseStmt defaultCase;

    SwitchCaseExpr(Expression expr, List<CaseStmt> cases, CaseStmt defaultCase, LineFile lineFile) {
        super(lineFile);

        this.expr = expr;
        this.cases = cases;
        this.defaultCase = defaultCase;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        for (CaseStmt caseStmt: cases) {
            boolean caseCondition = caseStmt.evalCondition(env);
            if (caseCondition) {
                CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env, true);
                SplElement result = caseStmt.bodyBlock.evaluate(blockEnv);
                if (result == null) {
                    result = blockEnv.yieldResult();
                }
                return result == null ? Pointer.NULL_PTR : result;
            }
        }
        if (defaultCase != null) {
            CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env, true);
            SplElement result = defaultCase.bodyBlock.evaluate(blockEnv);
            if (result == null) {
                result = blockEnv.yieldResult();
            }
            return result == null ? Pointer.NULL_PTR : result;
        } else {
            return Pointer.NULL_PTR;
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
