package interpreter.env;

import interpreter.primitives.Pointer;

public class TryEnvironment extends SubAbstractEnvironment {

    private Pointer exceptionPtr;

    public TryEnvironment(Environment outer) {
        super(outer);
    }

    @Override
    public void throwException(Pointer exceptionPtr) {
        this.exceptionPtr = exceptionPtr;
    }

    public Pointer getExceptionPtr() {
        return exceptionPtr;
    }

    @Override
    public boolean interrupted() {
        return outer.interrupted();
    }

    @Override
    public void breakLoop() {
        outer.breakLoop();
    }

    @Override
    public void resumeLoop() {
        outer.resumeLoop();
    }

    @Override
    public void pauseLoop() {
        outer.pauseLoop();
    }

    @Override
    public void invalidate() {
        throw new EnvironmentError();
    }
}
