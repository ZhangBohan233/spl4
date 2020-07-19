package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class VoidNode extends LeafNode {

    public static final VoidNode VOID_NODE = new VoidNode(LineFile.LF_PARSER);

    public VoidNode(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }

}
