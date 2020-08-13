package spl.lexer;

import spl.util.LineFile;

public class FloatToken extends Token {

    private double value;
//    private byte[] bytes = new byte[8];

    public FloatToken(String numString, LineFile lineFile) {
        super(lineFile);

        value = Double.parseDouble(numString);
//        Bytes.doubleToBytes(d, bytes, 0);
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "FloatToken{" + value + "}";
    }
}
