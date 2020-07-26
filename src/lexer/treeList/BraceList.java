package lexer.treeList;

import util.LineFile;

public class BraceList extends CollectiveElement {

    public BraceList(CollectiveElement parentElement, LineFile lineFile) {
        super(parentElement, lineFile);
    }

    @Override
    public String toString() {
        return "Brace{" + elements + "}";
    }
}
