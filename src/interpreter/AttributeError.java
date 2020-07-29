package interpreter;

import interpreter.splErrors.NativeError;
import util.LineFile;

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
