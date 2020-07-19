package ast;

import util.LineFile;

public abstract class ConditionalStmt extends Node {

    protected BlockStmt bodyBlock;

    public ConditionalStmt(LineFile lineFile) {
        super(lineFile);
    }

    public void setBodyBlock(BlockStmt bodyBlock) {
        this.bodyBlock = bodyBlock;
    }

    public BlockStmt getBodyBlock() {
        return bodyBlock;
    }
}
