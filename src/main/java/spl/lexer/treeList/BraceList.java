package spl.lexer.treeList;

import spl.util.LineFilePos;

public class BraceList extends CollectiveElement {

    public BraceList(CollectiveElement parentElement, LineFilePos lineFile) {
        super(parentElement, lineFile);
    }

    @Override
    public String toString() {
        return "Brace{" + elements + "}";
    }
}
