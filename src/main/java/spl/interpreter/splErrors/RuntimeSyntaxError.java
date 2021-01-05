package spl.interpreter.splErrors;

import spl.util.LineFilePos;

public class RuntimeSyntaxError extends NativeError {

    public RuntimeSyntaxError() {
        super();
    }

    public RuntimeSyntaxError(String msg) {
        super(msg, LineFilePos.LF_INTERPRETER);
    }

    public RuntimeSyntaxError(String msg, LineFilePos lineFile) {
        super(msg, lineFile);
    }
}
