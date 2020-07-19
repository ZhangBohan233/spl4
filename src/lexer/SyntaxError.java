package lexer;

import util.LineFile;
import util.SplBaseException;

public class SyntaxError extends SplBaseException {

    public SyntaxError(String msg, LineFile location) {
        super(msg + location.toStringFileLine());
    }
}
