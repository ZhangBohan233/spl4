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

public class CondCaseStmt extends Node {

    private final List<CaseStmt> cases = new ArrayList<>();
    private BlockStmt defaultCase;

    public CondCaseStmt(LineFile lineFile) {
        super(lineFile);
    }

    public void setCases(BlockStmt body) {
        for (Line line : body.getLines()) {
            if (line.getChildren().size() == 1) {
                Node node = line.getChildren().get(0);
                if (node instanceof CaseStmt) {
                    CaseStmt caseStmt = (CaseStmt) node;
                    if (caseStmt.isDefault) {
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
            } else {
                throw new ParseError("Unexpected case content. ", getLineFile());
            }
        }
    }

    @Override
    protected SplElement internalEval(Environment env) {
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
        if (execDefault) {
            CaseBlockEnvironment blockEnv = new CaseBlockEnvironment(env);
            defaultCase.evaluate(blockEnv);
        }
        return null;
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
