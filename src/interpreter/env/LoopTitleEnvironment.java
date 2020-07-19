package interpreter.env;

public class LoopTitleEnvironment extends SubAbstractEnvironment {

    private boolean broken;
    private boolean paused;

    public LoopTitleEnvironment(Environment outer) {
        super(outer);
    }

    @Override
    public boolean interrupted() {
        return broken || paused || outer.interrupted();
    }

    @Override
    public void breakLoop() {
        broken = true;
    }

    @Override
    public void resumeLoop() {
        paused = false;
    }

    @Override
    public void pauseLoop() {
        paused = true;
    }

    public void invalidate() {
        throw new EnvironmentError();
    }

    public boolean isBroken() {
        return broken;
    }
}
