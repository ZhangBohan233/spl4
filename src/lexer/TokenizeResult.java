package lexer;

import lexer.treeList.BraceList;
import lexer.treeList.CollectiveElement;

public class TokenizeResult {

    public final CollectiveElement rootList;

    public TokenizeResult(CollectiveElement rootList) {
        this.rootList = rootList;
    }
}
