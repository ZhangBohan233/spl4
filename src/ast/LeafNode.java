package ast;

import util.LineFile;

public abstract class LeafNode extends Node {
    public LeafNode(LineFile lineFile) {
        super(lineFile);
    }
}
