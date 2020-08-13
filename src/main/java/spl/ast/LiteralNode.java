package spl.ast;

import spl.util.LineFile;

public abstract class LiteralNode extends AbstractExpression {
    public LiteralNode(LineFile lineFile) {
        super(lineFile);
    }
}
