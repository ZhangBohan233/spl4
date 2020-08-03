package ast;

import interpreter.env.CaseBlockEnvironment;
import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splErrors.NativeError;
import util.LineFile;

import java.util.List;

public class CondCaseExpr extends AbstractExpression {

    private final List<CaseStmt> cases;
    private final CaseStmt defaultCase;

    CondCaseExpr(List<CaseStmt> cases, CaseStmt defaultCase, LineFile lineFile) {
        super(lineFile);

        this.cases = cases;
        this.defaultCase = defaultCase;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        for (CaseStmt caseStmt: cases) {
            Bool caseCondition = Bool.evalBoolean(caseStmt.getCondition(), env, getLineFile());
            if (caseCondition.value) {
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
