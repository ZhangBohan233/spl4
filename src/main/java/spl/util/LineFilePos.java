package spl.util;

import java.io.File;

public class LineFilePos {

    public static final LineFilePos LF_TOKENIZER = new LineFilePos("Tokenizer");
    public static final LineFilePos LF_INTERPRETER = new LineFilePos("Interpreter");
    public static final LineFilePos LF_PARSER = new LineFilePos("Parser");
    public static final LineFile LF_CONSOLE = new LineFile("Console");
    public static final LineFilePos LFP_CONSOLE = new LineFilePos(LF_CONSOLE, 0);

    public final int pos;
    public final LineFile lineFile;

    public LineFilePos(String msg) {
        this(new LineFile(msg), 0);
    }

    public LineFilePos(LineFile lineFile, int pos) {
        this.lineFile = lineFile;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public int getLine() {
        return lineFile.line;
    }

    public File getFile() {
        return lineFile.file;
    }

    public String getMsg() {
        return lineFile.msg;
    }

    public String toStringFileLine() {
        if (lineFile.msg == null) {
            return String.format("In file '%s', at %d:%d.", lineFile.file, lineFile.line, pos);
        } else {
            return String.format("In '%s'", lineFile.msg);
        }
    }

    @Override
    public String toString() {
        return toStringFileLine();
    }

    public static class LineFile {
        public final int line;
        public final File file;
        public final String msg;

        public LineFile(int line, File file) {
            this.line = line;
            this.file = file;
            this.msg = null;
        }

        public LineFile(String msg) {
            this.line = 0;
            this.file = null;
            this.msg = msg;
        }
    }
}
