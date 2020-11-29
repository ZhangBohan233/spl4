package spl.ast;

import spl.util.LineFile;

/**
 * This class is the superclass of all spl node that evaluates to non-null value.
 */
public abstract class Expression extends Node {

    public Expression(LineFile lineFile) {
        super(lineFile);
    }
}
