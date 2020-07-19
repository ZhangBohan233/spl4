package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class FallthroughStmt extends LeafNode {

    public FallthroughStmt(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        env.fallthrough();
        return null;
    }
}
