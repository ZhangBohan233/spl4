package lexer;

import util.LineFile;

public class CharToken extends Token {

    private char ch;

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
