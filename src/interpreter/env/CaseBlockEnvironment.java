package interpreter.env;

import interpreter.primitives.SplElement;
import interpreter.splErrors.RuntimeSyntaxError;
import util.LineFile;

public class CaseBlockEnvironment extends BlockEnvironment {

    private final boolean isExpr;
    private boolean fallthrough = false;
    private SplElement yieldResult;

    public CaseBlockEnvironment(Environment outer, boolean isExpr) {
        super(outer);

        this.isExpr = isExpr;
    }

    @Override
    public void fallthrough(LineFile lineFile) {
        if (isExpr) {
            throw new RuntimeSyntaxError("'fallthrough' outside cond/switch statements. ", lineFile);
        }
        fallthrough = true;
    }

    @Override
    public boolean isFallingThrough() {
        return fallthrough;
    }

    @Override
    public void yield(SplElement value, LineFile lineFile) {
        if (!isExpr) {
            throw new RuntimeSyntaxError("'yield' outside cond/switch expressions. ", lineFile);
        }
        this.yieldResult = value;
    }

    @Override
    public SplElement yieldResult() {
        return yieldResult;
    }
}
