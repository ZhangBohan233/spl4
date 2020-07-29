package ast;

import interpreter.env.Environment;
import interpreter.env.TryEnvironment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class TryStmt extends AbstractStatement {

    private final List<CatchStmt> catchStmts = new ArrayList<>();
    private final BlockStmt body;
    private BlockStmt finallyBlock;

    public TryStmt(BlockStmt body, LineFile lineFile) {
        super(lineFile);

        this.body = body;
    }

    public void addCatch(CatchStmt catchStmt) {
        catchStmts.add(catchStmt);
    }

    public void setFinallyBlock(BlockStmt finallyBlock) {
        this.finallyBlock = finallyBlock;
    }

    @Override
    protected void internalProcess(Environment env) {
        try {
            TryEnvironment tryEnv = new TryEnvironment(env);
            body.evaluate(tryEnv);
            if (tryEnv.hasException()) {
                Pointer exceptionPtr = tryEnv.getExceptionPtr();
            }
        } catch (Exception e) {

        } finally {

        }
    }

    @Override
    public String toString() {
        return String.format("try %s %s finally %s", body, catchStmts, finallyBlock);
    }

    private ExceptionContainer[][] evalExceptions() {
        ExceptionContainer[][] containers = new ExceptionContainer[catchStmts.size()][];
        for (CatchStmt catchStmt : catchStmts) {

        }
        return null;
    }

    private static int getExprSize(AbstractExpression expr) {
        if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            return getExprSize(binaryExpr.left) + getExprSize(binaryExpr.right);
        } else return 1;
    }

    private static class ExceptionContainer {

    }
}
