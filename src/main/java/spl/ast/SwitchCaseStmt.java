package spl.ast;

import spl.interpreter.env.CaseBlockEnvironment;
import spl.interpreter.env.Environment;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.util.List;

public class SwitchCaseStmt extends Statement {

    private final List<CaseStmt> cases;
    private final CaseStmt defaultCase;

    SwitchCaseStmt(List<CaseStmt> cases, CaseStmt defaultCase, LineFilePos lineFile) {
        super(lineFile);

        this.cases = cases;
        this.defaultCase = defaultCase;
    }

    public static SwitchCaseStmt reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        List<CaseStmt> cases = in.readList();
        boolean hasDefault = in.readBoolean();
        CaseStmt defaultCase = null;
        if (hasDefault) defaultCase = Reconstructor.reconstruct(in);
        return new SwitchCaseStmt(cases, defaultCase, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeList(cases);
        out.writeBoolean(defaultCase != null);
        if (defaultCase != null) defaultCase.save(out);
    }

    @Override
    protected void internalProcess(Environment env) {
        boolean execDefault = true;
        for (CaseStmt caseStmt : cases) {
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
