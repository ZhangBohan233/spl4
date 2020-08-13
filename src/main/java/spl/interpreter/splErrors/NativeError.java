package spl.interpreter.splErrors;

import spl.util.LineFile;
import spl.util.SplBaseException;

public class NativeError extends SplBaseException {

    public NativeError() {
        super();
    }

    public NativeError(Throwable cause) {
        super(cause);
    }

    public NativeError(LineFile lineFile) {
        super(lineFile);
    }

    public NativeError(String msg) {
        super(msg, LineFile.LF_INTERPRETER);
    }

    public NativeError(String msg, LineFile lineFile) {
        super(msg + lineFile.toStringFileLine());
    }
}
