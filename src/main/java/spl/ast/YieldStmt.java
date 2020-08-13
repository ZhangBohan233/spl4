package spl.ast;

import spl.interpreter.env.Environment;
import spl.util.LineFile;

public class YieldStmt extends UnaryStmt {

    public YieldStmt(LineFile lineFile) {
        super("yield", true, lineFile);
    }

    @Override
    protected void internalProcess(Environment env) {
        env.yield(value.evaluate(env), lineFile);
    }
}
