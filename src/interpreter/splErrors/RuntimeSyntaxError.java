package interpreter.splErrors;

import util.LineFile;

public class RuntimeSyntaxError extends NativeError {

    public RuntimeSyntaxError() {
        super();
    }

    public RuntimeSyntaxError(String msg) {
        super(msg, LineFile.LF_INTERPRETER);
    }

    public RuntimeSyntaxError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }
}
