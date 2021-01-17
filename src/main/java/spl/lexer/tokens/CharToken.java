package spl.lexer.tokens;

import spl.util.LineFilePos;

import java.util.Map;

public class CharToken extends LiteralToken {

    public final static char[] ESCAPE_CHARS = {'\f', '\n', '\t', '\r', '\b', '\0'};
    public final static Map<Character, Character> ESCAPES = Map.of(
            'f', '\f',
            'n', '\n',
            't', '\t',
            'r', '\r',
            'b', '\b',
            '0', '\0'
    );

    private final char ch;

    public CharToken(char ch, LineFilePos lineFile) {
        super(lineFile);
        this.ch = ch;
    }

    public char getValue() {
        return ch;
    }

    @Override
    public String toString() {
        return "CharToken{" +
                ch +
                '}';
    }
}
