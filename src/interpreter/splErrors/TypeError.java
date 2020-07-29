package interpreter.splErrors;

import interpreter.splErrors.NativeError;
import util.LineFile;

public class TypeError extends NativeError {

    public TypeError() {
        super();
    }

    public TypeError(String msg) {
        super(msg, LineFile.LF_INTERPRETER);
    }

    public TypeError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }
}
