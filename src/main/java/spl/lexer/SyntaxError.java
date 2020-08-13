package spl.lexer;

import spl.util.LineFile;
import spl.util.SplBaseException;

public class SyntaxError extends SplBaseException {

    public SyntaxError(String msg, LineFile location) {
        super(msg + location.toStringFileLine());
    }
}
