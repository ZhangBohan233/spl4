package ast;

import util.LineFile;

public abstract class Expr extends Node {

    public Expr(LineFile lineFile) {
        super(lineFile);
    }

    public abstract boolean notFulfilled();
}
