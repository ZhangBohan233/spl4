package spl.lexer.treeList;

import spl.util.LineFilePos;

public class ArrowBracketList extends CollectiveElement {
    public ArrowBracketList(CollectiveElement parentElement, LineFilePos lineFile) {
        super(parentElement, lineFile);
    }

    @Override
    public String toString() {
        return "ArrowBracket<" + elements + ">";
    }
}
