package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class BreakStmt extends LeafNode {

    public BreakStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        env.breakLoop();
        return null;
    }

}
