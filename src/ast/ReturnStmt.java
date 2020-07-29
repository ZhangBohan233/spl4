package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class ReturnStmt extends UnaryStmt {

    public ReturnStmt(LineFile lineFile) {
        super("return", true, lineFile);
    }

    @Override
    public boolean voidAble() {
        return true;
    }

    @Override
    protected void internalProcess(Environment env) {
        env.setReturn(value.evaluate(env));
    }
}
