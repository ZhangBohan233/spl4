package spl.lexer.treeList;

import spl.util.LineFilePos;

public class BracketList extends CollectiveElement {
    public BracketList(CollectiveElement parentElement, LineFilePos lineFile) {
        super(parentElement, lineFile);
    }

    @Override
    public String toString() {
        return "Bracket(" + elements + ")";
    }
}
