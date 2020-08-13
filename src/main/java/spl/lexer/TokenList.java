package spl.lexer;

import java.util.List;

public class TokenList {

    private final List<Token> tokens;

    TokenList(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public String toString() {
        return tokens.toString();
    }
}
