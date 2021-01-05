package spl.interpreter.env;

import spl.util.LineFilePos;
import spl.util.SplBaseException;

public class EnvironmentError extends SplBaseException {

    public EnvironmentError() {
        super();
    }

    public EnvironmentError(String msg) {
        super(msg);
    }

    public EnvironmentError(String msg, LineFilePos lineFile) {
        super(msg, lineFile);
    }
}
