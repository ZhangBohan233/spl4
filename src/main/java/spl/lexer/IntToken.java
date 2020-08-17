package spl.lexer;

import spl.util.LineFile;

public class IntToken extends Token {

    private final long value;
//    private byte[] bytes = new byte[8];

    public IntToken(String numStr, LineFile lineFile) {
        super(lineFile);

        value = Long.parseLong(numStr.replace("_", ""));
//        Bytes.longToBytes(num, bytes, 0);
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "IntToken{" + value + "}";
    }
}