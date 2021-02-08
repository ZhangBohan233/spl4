package spl.lexer.tokens;

import spl.util.LineFilePos;

public class AnnotationToken extends Token {
    private final String annotation;

    public AnnotationToken(String annotation, LineFilePos lineFile) {
        super(lineFile);

        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    @Override
    public String toString() {
        return "@" + annotation;
    }
}
