package spl.ast;

import spl.util.LineFile;

public abstract class LiteralNode extends Expression {
    public LiteralNode(LineFile lineFile) {
        super(lineFile);
    }
}
