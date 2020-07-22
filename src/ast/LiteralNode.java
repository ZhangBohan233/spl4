package ast;

import util.LineFile;

public abstract class LiteralNode extends AbstractExpression {
    public LiteralNode(LineFile lineFile) {
        super(lineFile);
    }
}
