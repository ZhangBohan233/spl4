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

    private final List<CaseStmt> cases = new ArrayList<>();
    private BlockStmt defaultCase;

    public CondCaseStmt(BlockStmt bodyBlock, LineFile lineFile) {
        super(lineFile);

        setCases(bodyBlock);
    }

    private void setCases(BlockStmt body) {
        if (body.getLines().size() > 0) {
            Line line0 = body.getLines().get(0);
            for (Node node : line0.getChildren()) {
                if (node instanceof CaseStmt) {
                    CaseStmt caseStmt = (CaseStmt) node;
                    if (caseStmt.isDefault()) {
                        if (defaultCase == null) {
                            defaultCase = caseStmt.bodyBlock;
                        } else {
                            throw new ParseError("Multiple default cases. ", getLineFile());
                        }
                    } else {
                        cases.add(caseStmt);
                    }
                } else {
                    throw new ParseError("'cond' statement must only contain 'case' statementes.",
                            getLineFile());
                }
            }
        }
    }

    @Override
    protected void internalProcess(Environment env) {
        boolean execDefault = true;
        for (CaseStmt caseStmt: cases) {
            Bool caseCondition = Bool.evalBoolean(caseStmt.getCondition(), env, getLineFile());
            if (caseCondition.value) {
                CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env);
                caseStmt.evaluate(blockEnv);
                if (!blockEnv.isFallingThrough()) {
                    execDefault = false;
                    break;
                }
            }
        }
        if (execDefault && defaultCase != null) {
            CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env);
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
