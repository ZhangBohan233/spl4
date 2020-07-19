package lexer.treeList;

public class BracketList extends CollectiveElement {
    public BracketList(CollectiveElement parentElement) {
        super(parentElement);
    }

    @Override
    public String toString() {
        return "Bracket(" + elements + ")";
    }
}
