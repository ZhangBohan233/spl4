package spl.util;

public class SplBaseException extends RuntimeException {

    public SplBaseException() {
        super();
    }

    public SplBaseException(LineFilePos lineFile) {
        super(lineFile.toStringFileLine());
    }

    public SplBaseException(Throwable cause) {
        super(cause);
    }

    public SplBaseException(String msg) {
        super(msg);
    }

    public SplBaseException(String msg, LineFilePos lineFile) {
        super(msg + lineFile.toStringFileLine());
    }
}
