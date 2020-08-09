package interpreter.env;

import interpreter.splErrors.NativeError;
import util.LineFile;
import util.SplBaseException;

public class EnvironmentError extends NativeError {

    public EnvironmentError() {
        super();
    }

    public EnvironmentError(String msg) {
        super(msg);
    }

    public EnvironmentError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }
}
