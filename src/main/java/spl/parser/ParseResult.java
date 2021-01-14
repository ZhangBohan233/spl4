package spl.parser;

import spl.ast.BlockStmt;

import java.util.Map;

public class ParseResult {

    private final BlockStmt root;

    public ParseResult(BlockStmt root) {
        this.root = root;
    }

    public BlockStmt getRoot() {
        return root;
    }

}
