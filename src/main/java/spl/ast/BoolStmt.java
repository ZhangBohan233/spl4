package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public class BoolStmt extends LiteralNode {

    private final Bool value;

    public BoolStmt(boolean val, LineFile lineFile) {
        super(lineFile);

        value = Bool.boolValueOf(val);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
