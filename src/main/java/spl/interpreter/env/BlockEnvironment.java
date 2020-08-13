package spl.interpreter.env;


public class BlockEnvironment extends SubAbstractEnvironment {

    public BlockEnvironment(Environment outer) {
        super(outer);
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

    public void invalidate() {
        variables.clear();
    }
}
