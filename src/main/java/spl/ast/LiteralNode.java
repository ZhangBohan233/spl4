package spl.ast;

import spl.util.LineFilePos;

public abstract class LiteralNode extends Expression {
    public LiteralNode(LineFilePos lineFile) {
        super(lineFile);
    }
}
