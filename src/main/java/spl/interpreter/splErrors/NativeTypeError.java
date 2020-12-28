package spl.interpreter.splErrors;

import spl.util.LineFile;

public class NativeTypeError extends NativeError {

    public NativeTypeError() {
        super();
    }

    public NativeTypeError(LineFile lineFile) {
        super(lineFile);
    }

    public NativeTypeError(String msg) {
        super(msg, LineFile.LF_INTERPRETER);
    }

    public NativeTypeError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }
}
