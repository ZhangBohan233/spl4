package interpreter.env;

public class CaseBlockEnvironment extends BlockEnvironment {
    private boolean fallthrough = false;

    public CaseBlockEnvironment(Environment outer) {
        super(outer);
    }

    @Override
    public void fallthrough() {
        fallthrough = true;
    }

    @Override
    public boolean isFallingThrough() {
        return fallthrough;
    }
}
