package spl.lexer;

import spl.util.LineFilePos;

public class FloatToken extends Token {

    private final double value;

    public FloatToken(String dotFront, String dotBack, LineFilePos lineFile) {
        super(lineFile);

        long dec = IntToken.parse(dotFront);
        value = dec + Double.parseDouble("0." + dotBack);
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "FloatToken{" + value + "}";
    }
}
