package parser;

import util.LineFile;

public class ParseError extends RuntimeException {

    public ParseError(String msg, LineFile location) {
        super(msg + location.toStringFileLine());
    }
}
