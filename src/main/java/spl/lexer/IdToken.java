package spl.lexer;

import spl.util.LineFile;

public class IdToken extends Token {

    private String identifier;

    public IdToken(String identifier, LineFile lineFile) {
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
