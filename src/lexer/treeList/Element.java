package lexer.treeList;

public abstract class Element {

    public final CollectiveElement parentElement;

    public Element(CollectiveElement parentElement) {
        this.parentElement = parentElement;
    }
}
