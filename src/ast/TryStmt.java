package ast;

import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public class TryStmt extends Node {

    private final List<CatchStmt> catchStmts = new ArrayList<>();
    private final BlockStmt body;
    private BlockStmt finallyBlock;

    public TryStmt(BlockStmt body, LineFile lineFile) {
        super(lineFile);

        this.body = body;
    }

    public void addCatch(CatchStmt catchStmt) {
        catchStmts.add(catchStmt);
    }

    public void setFinallyBlock(BlockStmt finallyBlock) {
        this.finallyBlock = finallyBlock;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return null;
    }

    @Override
    public String toString() {
        return String.format("try %s %s finally %s", body, catchStmts, finallyBlock);
    }
}
