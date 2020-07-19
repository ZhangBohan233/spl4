package lexer;

import lexer.treeList.BraceList;

public class TokenizeResult {

    public final BraceList rootList;

    public TokenizeResult(BraceList rootList) {
        this.rootList = rootList;
    }
}
