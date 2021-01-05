package spl.interpreter.splErrors;

import spl.util.LineFilePos;
import spl.util.SplBaseException;

public class NativeError extends SplBaseException {

    public NativeError() {
        super();
    }

    public NativeError(Throwable cause) {
        super(cause);
    }

    public NativeError(LineFilePos lineFile) {
        super(lineFile);
    }

    public NativeError(String msg) {
        super(msg, LineFilePos.LF_INTERPRETER);
    }

    public NativeError(String msg, LineFilePos lineFile) {
        super(msg + lineFile.toStringFileLine());
    }
}
