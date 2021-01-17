package spl.lexer.tokens;

import spl.util.LineFilePos;

public class ByteToken extends LiteralToken {

    private final byte b;

    public ByteToken(String numStr, LineFilePos lineFile) {
        super(lineFile);

        this.b = (byte) IntToken.parse(numStr);
    }

    public byte getValue() {
        return b;
    }

    @Override
    public String toString() {
        return "ByteToken{" +
                b +
                '}';
    }
}
