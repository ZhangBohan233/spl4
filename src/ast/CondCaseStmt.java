package ast;

import interpreter.env.BlockEnvironment;
import interpreter.env.CaseBlockEnvironment;
import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import parser.ParseError;
import util.LineFile;
import util.Utilities;

import java.util.ArrayList;
import java.util.List;

public class CondCaseStmt extends AbstractStatement {

    private final List<CaseStmt> cases;
    private final CaseStmt defaultCase;

    CondCaseStmt(List<CaseStmt> cases, CaseStmt defaultCase, LineFile lineFile) {
        super(lineFile);

        this.cases = cases;
        this.defaultCase = defaultCase;
    }

    @Override
    protected void internalProcess(Environment env) {
        boolean execDefault = true;
        for (CaseStmt caseStmt: cases) {
            Bool caseCondition = Bool.evalBoolean(caseStmt.getCondition(), env, getLineFile());
            if (caseCondition.value) {
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
