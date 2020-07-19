package ast;

import util.LineFile;

public abstract class LiteralNode extends LeafNode {
    public LiteralNode(LineFile lineFile) {
        super(lineFile);
    }
}
