package spl.lexer.treeList;

import spl.util.LineFilePos;

public class SqrBracketList extends CollectiveElement {
    public SqrBracketList(CollectiveElement parentElement, LineFilePos lineFile) {
        super(parentElement, lineFile);
    }

    @Override
    public String toString() {
        return "SqrBracket[" + elements + "]";
    }
}
