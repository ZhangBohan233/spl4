package interpreter;

import util.LineFile;
import util.SplBaseException;

public class SplException extends SplBaseException {

    public SplException() {
        super();
    }

    public SplException(String msg) {
        super(msg, LineFile.LF_INTERPRETER);
    }

    public SplException(String msg, LineFile lineFile) {
        super(msg + lineFile.toStringFileLine());
    }
}
