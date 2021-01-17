package spl.lexer.tokens;

import spl.lexer.SyntaxError;
import spl.util.LineFilePos;

public class StrToken extends LiteralToken {

    private final String literal;

    public StrToken(String literal, LineFilePos lineFile) {
        super(lineFile);
        this.literal = replaceEscapes(literal, lineFile);
    }

    public String getLiteral() {
        return literal;
    }

    private static String replaceEscapes(String s, LineFilePos lineFile) {
        StringBuilder builder = new StringBuilder();
        int strLen = s.length();
        for (int i = 0; i < strLen; i++) {
            char cur = s.charAt(i);
            if (i < strLen - 1) {
                char next = s.charAt(i + 1);
                if (cur == '\\') {
                    if (next == '\\') {
                        builder.append('\\');
                        i += 1;
                        continue;
                    } else if (CharToken.ESCAPES.containsKey(next)) {
                        builder.append(CharToken.ESCAPES.get(next));
                        i += 1;
                        continue;
                    } else {
                        throw new SyntaxError("Invalid escape '" + cur + next + "'. ", lineFile);
                    }
                }
            }
            builder.append(cur);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "StrToken{" +
                literal +
                '}';
    }
}
