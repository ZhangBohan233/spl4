package spl.interpreter.env;

public class ThreadEnvironment extends ExceptionContainerEnv {
    private final int threadId;

    public ThreadEnvironment(Environment outer, int threadId) {
        super(outer.memory, outer);

        this.threadId = threadId;
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
