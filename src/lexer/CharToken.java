package lexer;

import util.LineFile;

import java.util.Map;

public class CharToken extends Token {

    public final static Map<Character, Character> ESCAPES = Map.of(
            'f', '\f',
            'n', '\n',
            't', '\t',
            'r', '\r',
            'b', '\b',
            '0', '\0'
    );

    private final char ch;

    public CharToken(char ch, LineFile lineFile) {
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
