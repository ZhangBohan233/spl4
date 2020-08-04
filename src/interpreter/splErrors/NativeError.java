package interpreter.splErrors;

import util.LineFile;
import util.SplBaseException;

public class NativeError extends SplBaseException {

    public NativeError() {
        super();
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
