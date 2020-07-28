package ast;

import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import util.LineFile;

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
