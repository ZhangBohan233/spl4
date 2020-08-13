package spl.lexer.treeList;

import spl.util.LineFile;

public class SqrBracketList extends CollectiveElement {
    public SqrBracketList(CollectiveElement parentElement, LineFile lineFile) {
        super(parentElement, lineFile);
    }

    @Override
    public String toString() {
        return "SqrBracket[" + elements + "]";
    }
}
