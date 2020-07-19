package interpreter;

import util.LineFile;

public class AttributeError extends SplException {

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
