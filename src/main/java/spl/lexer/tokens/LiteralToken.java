package spl.lexer.tokens;

import spl.util.LineFilePos;

public abstract class LiteralToken extends Token {
    public LiteralToken(LineFilePos lineFile) {
        super(lineFile);
    }
}
