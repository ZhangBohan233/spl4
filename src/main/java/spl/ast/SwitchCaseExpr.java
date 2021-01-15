package spl.ast;

import spl.interpreter.env.CaseBlockEnvironment;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class SwitchCaseExpr extends Expression {

    private final List<CaseStmt> cases;
    private final CaseStmt defaultCase;

    SwitchCaseExpr(List<CaseStmt> cases, CaseStmt defaultCase, LineFilePos lineFile) {
        super(lineFile);

        this.cases = cases;
        this.defaultCase = defaultCase;
    }

    public static SwitchCaseExpr reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        List<CaseStmt> cases = in.readList();
        boolean hasDefault = in.readBoolean();
        CaseStmt defaultCase = null;
        if (hasDefault) defaultCase = Reconstructor.reconstruct(in);
        return new SwitchCaseExpr(cases, defaultCase, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeList(cases);
        out.writeBoolean(defaultCase != null);
        if (defaultCase != null) defaultCase.save(out);
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
                return result == null ? Reference.NULL : result;
            }
        }
        if (defaultCase != null) {
            CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env, true);
            SplElement result = defaultCase.bodyBlock.evaluate(blockEnv);
            if (result == null) {
                result = blockEnv.yieldResult();
            }
            return result == null ? Reference.NULL : result;
        } else {
            return Reference.NULL;
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
