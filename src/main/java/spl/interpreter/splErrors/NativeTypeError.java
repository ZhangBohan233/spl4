package spl.interpreter.splErrors;

import spl.util.LineFilePos;

public class NativeTypeError extends NativeError {

    public NativeTypeError() {
        super();
    }

    public NativeTypeError(LineFilePos lineFile) {
        super(lineFile);
    }

    public NativeTypeError(String msg) {
        super(msg, LineFilePos.LF_INTERPRETER);
    }

    public NativeTypeError(String msg, LineFilePos lineFile) {
        super(msg, lineFile);
    }
}
