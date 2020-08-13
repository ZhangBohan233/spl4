package spl.ast;

import spl.parser.ParseError;
import spl.util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class CondCaseFactory {

    private final List<CaseStmt> cases = new ArrayList<>();
    private CaseStmt defaultCase = null;
    private int exprLevel = 0;  // 0 for not initialized, 1 for expr, 2 for stmt

    public CondCaseFactory(BlockStmt body) {

        analyze(body);
    }

    private void analyze(BlockStmt body) {
        if (body.getLines().size() > 0) {
            Line line0 = body.getLines().get(0);
            for (Node node : line0.getChildren()) {
                if (node instanceof CaseStmt) {
                    CaseStmt caseStmt = (CaseStmt) node;

                    // make sure cases are all expr or all stmt, not mixed
                    if (caseStmt.isExpr) {
                        if (exprLevel == 2)
                            throw new ParseError("Cases must all be expressions or " +
                                    "all be statements. ", caseStmt.lineFile);
                        else exprLevel = 1;
                    } else {
                        if (exprLevel == 1)
                            // prev case is expr, but current one is stmt
                            throw new ParseError("Cases must all be expressions or " +
                                    "all be statements. ", caseStmt.lineFile);
                        else exprLevel = 2;
                    }

                    if (caseStmt.isDefault()) {
                        if (defaultCase == null) {
                            defaultCase = caseStmt;
                        } else {
                            throw new ParseError("Multiple default cases. ", caseStmt.lineFile);
                        }
                    } else {
                        cases.add(caseStmt);
                    }
                } else {
                    throw new ParseError("'cond' statement must only contain 'case' statements.",
                            node.lineFile);
                }
            }
        }
    }

    /**
     * Note that an empty body (exprLevel == 0) implies a statement since it does not return anything.
     *
     * @return {@code true} if this is an {@code CondCaseExpr}
     */
    public boolean isExpr() {
        return exprLevel == 1;
    }

    public CondCaseExpr buildExpr(LineFile lineFile) {
        return new CondCaseExpr(cases, defaultCase, lineFile);
    }

    public CondCaseStmt buildStmt(LineFile lineFile) {
        return new CondCaseStmt(cases, defaultCase, lineFile);
    }
}
