package lexer;

import util.LineFile;

public abstract class Token {
    private LineFile lineFile;

    public Token(LineFile lineFile) {
        this.lineFile = lineFile;
    }

    public LineFile getLineFile() {
        return lineFile;
    }
}

