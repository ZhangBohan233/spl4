package lexer.treeList;

public class SqrBracketList extends CollectiveElement {
    public SqrBracketList(CollectiveElement parentElement) {
        super(parentElement);
    }

    @Override
    public String toString() {
        return "SqrBracket[" + elements + "]";
    }
}
