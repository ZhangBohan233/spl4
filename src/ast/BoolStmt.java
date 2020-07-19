package ast;

import interpreter.env.Environment;
import interpreter.primitives.Bool;
import interpreter.primitives.SplElement;
import util.LineFile;

public class BoolStmt extends LiteralNode {

    public static final BoolStmt BOOL_STMT_TRUE = new BoolStmt(true, LineFile.LF_INTERPRETER);
    public static final BoolStmt BOOL_STMT_FALSE = new BoolStmt(false, LineFile.LF_INTERPRETER);

    private final Bool value;

    public BoolStmt(boolean val, LineFile lineFile) {
        super(lineFile);

        value = Bool.boolValueOf(val);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return value;
    }

}
