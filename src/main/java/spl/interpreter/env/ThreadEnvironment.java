package spl.interpreter.env;

import spl.interpreter.primitives.Reference;

public class ThreadEnvironment extends ExceptionContainerEnv {
    private final int threadId;

    public ThreadEnvironment(Environment outer, int threadId) {
        super(outer.memory, outer);

        this.threadId = threadId;
    }

    @Override
    public Reference getExceptionInsPtr() {
        return exceptionInsPtr == null ? globalEnv.getExceptionInsPtr() : exceptionInsPtr;
    }

    @Override
    public boolean hasException() {
        return exceptionInsPtr != null || globalEnv.hasException();
    }

    @Override
    public boolean interrupted() {
        return outer.interrupted();
    }

    @Override
    public int getThreadId() {
        return threadId;
    }
}
