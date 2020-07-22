package ast;

import util.LineFile;

/**
 * This class is the superclass of all spl node that evaluates to non-null value.
 */
public abstract class AbstractExpression extends Node {

    public AbstractExpression(LineFile lineFile) {
        super(lineFile);
    }
}
