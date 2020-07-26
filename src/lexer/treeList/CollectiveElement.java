package lexer.treeList;

import util.LineFile;

import java.util.ArrayList;
import java.util.List;

public abstract class CollectiveElement extends Element {

    public final LineFile lineFile;
    protected final List<Element> elements = new ArrayList<>();

    public CollectiveElement(CollectiveElement parentElement, LineFile lineFile) {
        super(parentElement);

        this.lineFile = lineFile;
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
