package spl.interpreter.splErrors;

import spl.util.LineFile;

public class TypeError extends NativeError {

    public TypeError() {
        super();
    }

    public TypeError(LineFile lineFile) {
        super(lineFile);
    }

    public TypeError(String msg) {
        super(msg, LineFile.LF_INTERPRETER);
    }

    public TypeError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }
}
