package spl.lexer.treeList;

import spl.lexer.Token;

public class AtomicElement extends Element {

    public final Token atom;

    public AtomicElement(Token atom, CollectiveElement parentElement) {
        super(parentElement);

        this.atom = atom;
    }

    @Override
    public String toString() {
        return atom.toString();
    }
}
