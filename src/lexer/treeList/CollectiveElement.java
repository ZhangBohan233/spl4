package lexer.treeList;

import java.util.ArrayList;
import java.util.List;

public abstract class CollectiveElement extends Element {

    protected final List<Element> elements = new ArrayList<>();

    public CollectiveElement(CollectiveElement parentElement) {
        super(parentElement);
    }

    public void add(Element element) {
        elements.add(element);
    }

    public int size() {
        return elements.size();
    }

    public Element get(int index) {
        return elements.get(index);
    }
}
