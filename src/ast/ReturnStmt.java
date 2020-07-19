package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class ReturnStmt extends UnaryExpr {

    public ReturnStmt(LineFile lineFile) {
        super("return", true, lineFile);
    }

    @Override
    public boolean voidAble() {
        return true;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        env.setReturn(value.evaluate(env));
        return null;
    }
}
