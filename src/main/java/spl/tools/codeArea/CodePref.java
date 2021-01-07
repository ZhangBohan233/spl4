package spl.tools.codeArea;

public class CodePref {
    
    private boolean autoIndent = true;
    private boolean autoBackBrace = true;

    public static final char[][] FRONT_BACKS = {
            {'{', '}'},
            {'[', ']'},
            {'(', ')'}
    };

    public void setAutoBackBrace(boolean autoBackBrace) {
        this.autoBackBrace = autoBackBrace;
    }

    public void setAutoIndent(boolean autoIndent) {
        this.autoIndent = autoIndent;
    }

    public boolean isAutoBackBrace() {
        return autoBackBrace;
    }

    public boolean isAutoIndent() {
        return autoIndent;
    }

    public static class Builder {

        private final CodePref codePref = new CodePref();

        public Builder autoIndent(boolean value) {
            codePref.autoIndent = value;
            return this;
        }

        public Builder autoBackBrace(boolean value) {
            codePref.autoBackBrace = value;
            return this;
        }

        public CodePref build() {
            return codePref;
        }
    }
}
