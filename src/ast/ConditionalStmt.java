package ast;

import util.LineFile;

public abstract class ConditionalStmt extends AbstractStatement {

    protected BlockStmt bodyBlock;

    public ConditionalStmt(LineFile lineFile) {
        super(lineFile);
    }

    public ConditionalStmt(BlockStmt bodyBlock, LineFile lineFile) {
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
