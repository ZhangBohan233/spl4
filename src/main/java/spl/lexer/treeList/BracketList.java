package spl.lexer.treeList;

import spl.util.LineFile;

public class BracketList extends CollectiveElement {
    public BracketList(CollectiveElement parentElement, LineFile lineFile) {
        super(parentElement, lineFile);
    }

    @Override
    public String toString() {
        return "Bracket(" + elements + ")";
    }
}
