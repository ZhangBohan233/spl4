package ast;

import util.LineFile;

public abstract class Expr extends AbstractExpression {

    public Expr(LineFile lineFile) {
        super(lineFile);
    }

    public abstract boolean notFulfilled();
}
