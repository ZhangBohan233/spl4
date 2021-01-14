package spl.ast;

import spl.util.LineFilePos;

public abstract class ConditionalStmt extends Statement {

    protected final BlockStmt bodyBlock;

    public ConditionalStmt(BlockStmt bodyBlock, LineFilePos lineFile) {
        super(lineFile);

        this.bodyBlock = bodyBlock;
    }
}
