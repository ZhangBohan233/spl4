package spl.util;

import java.io.File;

public class LineFile {

    public static final LineFile LF_TOKENIZER = new LineFile("Tokenizer");
    public static final LineFile LF_INTERPRETER = new LineFile("Interpreter");
    public static final LineFile LF_PARSER = new LineFile("Parser");
    public static final LineFile LF_CONSOLE = new LineFile("Console");

    public final int line;
    public final String msg;
    public final File file;

    public LineFile(String msg) {
        this.line = 0;
        this.msg = msg;
        this.file = null;
    }

    public LineFile(int line, File file) {
        this.line = line;
        this.msg = null;
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public File getFile() {
        return file;
    }

    public String toStringFileLine() {
        if (msg == null) {
            return String.format("In file '%s', at line %d.", file, line);
        } else {
            return String.format("In '%s'", msg);
        }
    }
}
