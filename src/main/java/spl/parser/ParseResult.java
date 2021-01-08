package spl.parser;

import spl.ast.BlockStmt;

import java.util.Map;

public class ParseResult {

    private BlockStmt root;

    ParseResult(BlockStmt root) {
        this.root = root;
    }

    public BlockStmt getRoot() {
        return root;
    }

}
