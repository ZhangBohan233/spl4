package spl.lexer.treeList;

import spl.util.LineFile;

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

    public void set(int index, Element element) {
        elements.set(index, element);
    }

    public Element get(int index) {
        return elements.get(index);
    }
}