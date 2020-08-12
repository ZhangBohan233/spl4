package lexer;

import util.LineFile;

public abstract class Token {
    public final LineFile lineFile;

    public Token(LineFile lineFile) {
        this.lineFile = lineFile;
    }

    public LineFile getLineFile() {
        return lineFile;
    }
}

