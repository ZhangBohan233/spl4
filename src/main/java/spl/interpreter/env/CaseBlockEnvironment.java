package spl.interpreter.env;

import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.RuntimeSyntaxError;
import spl.util.Constants;
import spl.util.LineFilePos;

public class CaseBlockEnvironment extends BlockEnvironment {

    private final boolean isExpr;
    private boolean fallthrough = false;
    private SplElement yieldResult;

    public CaseBlockEnvironment(Environment outer, boolean isExpr) {
        super(outer);

        this.isExpr = isExpr;
    }

    @Override
    public void fallthrough(LineFilePos lineFile) {
        if (isExpr) {
            SplInvokes.throwException(
                    this,
                    Constants.RUNTIME_SYNTAX_ERROR,
                    "'fallthrough' outside cond/switch statements.",
                    lineFile
            );
            return;
        }
        fallthrough = true;
    }

    @Override
    public boolean isFallingThrough() {
        return fallthrough;
    }

    @Override
    public void yield(SplElement value, LineFilePos lineFile) {
        if (!isExpr) {
            SplInvokes.throwException(
                    this,
                    Constants.RUNTIME_SYNTAX_ERROR,
                    "'yield' outside cond/switch expressions.",
                    lineFile
            );
            return;
        }
        this.yieldResult = value;
    }

    @Override
    public SplElement yieldResult() {
        return yieldResult;
    }
}
