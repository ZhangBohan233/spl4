package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFilePos;

public class ReturnStmt extends UnaryStmt {

    public ReturnStmt(LineFilePos lineFile) {
        super("return", true, lineFile);
    }

    @Override
    public boolean voidAble() {
        return true;
    }

    @Override
    protected void internalProcess(Environment env) {
        env.setReturn(value.evaluate(env), lineFile);
    }
}
