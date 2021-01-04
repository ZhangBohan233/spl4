package spl.interpreter.env;

import spl.interpreter.primitives.Reference;
import spl.util.LineFile;

public class TryEnvironment extends SubAbstractEnvironment {

    private Reference exceptionPtr;

    public TryEnvironment(Environment outer) {
        super(outer);
    }

    @Override
    public void throwException(Reference exceptionPtr) {
        this.exceptionPtr = exceptionPtr;
    }

    public Reference getExceptionPtr() {
        return exceptionPtr;
    }

    @Override
    public boolean hasException() {
        return exceptionPtr != null;
    }

    @Override
    public boolean interrupted() {
        return outer.interrupted();
    }

    @Override
    public void breakLoop(LineFile lineFile) {
        outer.breakLoop(lineFile);
    }

    @Override
    public void resumeLoop() {
        outer.resumeLoop();
    }

    @Override
    public void pauseLoop(LineFile lineFile) {
        outer.pauseLoop(lineFile);
    }

    @Override
    public void invalidate() {
        throw new EnvironmentError();
    }
}
