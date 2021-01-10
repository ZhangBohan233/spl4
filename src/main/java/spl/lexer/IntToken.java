package spl.lexer;

import spl.util.LineFilePos;

public class IntToken extends Token {

    private final long value;

    public IntToken(String numStr, LineFilePos lineFile) {
        super(lineFile);

        value = parse(numStr);
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "IntToken{" + value + "}";
    }

    static long parse(String numStr) {
        return Long.parseLong(numStr.replace("_", ""));
    }
}
