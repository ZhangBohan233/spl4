package spl.ast;

import spl.util.LineFilePos;

public abstract class ConditionalStmt extends Statement {

    protected BlockStmt bodyBlock;

    public ConditionalStmt(LineFilePos lineFile) {
        super(lineFile);
    }

    public ConditionalStmt(BlockStmt bodyBlock, LineFilePos lineFile) {
        super(lineFile);

        this.bodyBlock = bodyBlock;
    }

    public void setBodyBlock(BlockStmt bodyBlock) {
        this.bodyBlock = bodyBlock;
    }

    public BlockStmt getBodyBlock() {
        return bodyBlock;
    }
}
