package lexer.treeList;

public class BraceList extends CollectiveElement {

    public BraceList(CollectiveElement parentElement) {
        super(parentElement);
    }

    @Override
    public String toString() {
        return "Brace{" + elements + "}";
    }
}
