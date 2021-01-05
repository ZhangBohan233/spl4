package spl.lexer;

import spl.util.LineFilePos;

public abstract class Token {
    public final LineFilePos lineFile;

    public Token(LineFilePos lineFile) {
        this.lineFile = lineFile;
    }

    public LineFilePos getLineFile() {
        return lineFile;
    }
}

