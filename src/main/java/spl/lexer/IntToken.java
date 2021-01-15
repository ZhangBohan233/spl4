package spl.lexer;

import spl.util.LineFilePos;

public class IntToken extends Token {

    private final long value;

    public IntToken(String numStr, LineFilePos lineFile) {
        super(lineFile);

        value = parseWithRadix(numStr);
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "IntToken{" + value + "}";
    }

    private static long parseWithRadix(String numStr) {
        numStr = numStr.replace("_", "");
        if (numStr.startsWith("0x")) {
            return Long.parseLong(numStr.substring(2), 16);
        } else if (numStr.startsWith("0d")) {
            return Long.parseLong(numStr.substring(2), 10);
        } else if (numStr.startsWith("0o")) {
            return Long.parseLong(numStr.substring(2), 8);
        } else if (numStr.startsWith("0b")) {
            return Long.parseLong(numStr.substring(2), 2);
        } else {
            return Long.parseLong(numStr);
        }
    }

    static long parse(String numStr) {
        return Long.parseLong(numStr.replace("_", ""));
    }
}
