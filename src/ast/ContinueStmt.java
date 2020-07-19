package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class ContinueStmt extends LeafNode {

    public ContinueStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        env.pauseLoop();
        return null;
    }
}
