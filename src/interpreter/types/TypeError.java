package interpreter.types;

import interpreter.SplException;
import util.LineFile;

public class TypeError extends SplException {

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
