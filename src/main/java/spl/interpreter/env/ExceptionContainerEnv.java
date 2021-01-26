package spl.interpreter.env;

import spl.interpreter.Memory;
import spl.interpreter.primitives.Reference;

public abstract class ExceptionContainerEnv extends MainAbstractEnvironment {
    protected Reference exceptionInsPtr;

    public ExceptionContainerEnv(Memory memory, Environment outer) {
        super(memory, outer);
    }

    public void throwException(Reference exceptionInsPtr) {
        this.exceptionInsPtr = exceptionInsPtr;
    }

    public boolean hasException() {
        return exceptionInsPtr != null;
    }

    public Reference getExceptionInsPtr() {
        return exceptionInsPtr;
    }

    /**
     * Removes exception, to make all sub-environments work normally
     */
    public void removeException() {
        exceptionInsPtr = null;
    }
}
