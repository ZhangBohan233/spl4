package spl.parser;

import spl.util.LineFilePos;

public class ParseError extends RuntimeException {

    private final LineFilePos location;

    public ParseError(String msg, LineFilePos location) {
        super(msg + location.toStringFileLine());

        this.location = location;
    }

    public LineFilePos getLocation() {
        return location;
    }
}
