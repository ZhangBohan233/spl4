package spl.interpreter.splErrors;

import spl.util.LineFile;

public class AttributeError extends NativeError {

    public AttributeError() {
        super();
    }

    public AttributeError(String msg) {
        super(msg);
    }

    public AttributeError(String msg, LineFile lineFile) {
        super(msg, lineFile);
    }

}
