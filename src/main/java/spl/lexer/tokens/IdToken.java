package spl.lexer.tokens;

import spl.util.LineFilePos;

public class IdToken extends Token {

    private String identifier;

    public IdToken(String identifier, LineFilePos lineFile) {
        super(lineFile);
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "IdToken{" +
                identifier +
                '}';
    }

    public String getIdentifier() {
        return identifier;
    }
}
