package spl.lexer;

import spl.util.LineFilePos;
import spl.util.SplBaseException;

public class SyntaxError extends SplBaseException {

    public SyntaxError(String msg, LineFilePos location) {
        super(msg + location.toStringFileLine());
    }
}
