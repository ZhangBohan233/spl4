package spl.lexer.tokens;

import spl.util.LineFilePos;

public class DocToken extends LiteralToken {

    private final String doc;

    public DocToken(String doc, LineFilePos lineFile) {
        super(lineFile);

        this.doc = trim(doc);
    }

    private static String trim(String origDoc) {
        String[] lines = origDoc.split("\n");
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line.trim()).append('\n');
        }
        return builder.toString();
    }

    public String getDoc() {
        return doc;
    }
}
