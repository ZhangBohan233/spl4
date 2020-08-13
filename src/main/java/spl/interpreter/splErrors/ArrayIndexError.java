package spl.interpreter.splErrors;

import spl.util.LineFile;

public class ArrayIndexError extends NativeError {
    public ArrayIndexError() {
        super();
    }

    public ArrayIndexError(String msg) {
        super(msg, LineFile.LF_INTERPRETER);
    }

    public ArrayIndexError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }
}
