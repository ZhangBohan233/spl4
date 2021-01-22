package spl.lexer.tokens;

import spl.util.LineFilePos;

public class FloatToken extends LiteralToken {

    private final double value;

    public FloatToken(String dotFront, String dotBack, LineFilePos lineFile) {
        super(lineFile);

        long dec = IntToken.parse(dotFront);
        value = dec + Double.parseDouble("0." + dotBack.replace("_", ""));
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "FloatToken{" + value + "}";
    }
}
