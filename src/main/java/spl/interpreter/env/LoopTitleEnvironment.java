package spl.interpreter.env;

import spl.util.LineFilePos;

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
    public void breakLoop(LineFilePos lineFile) {
        broken = true;
    }

    @Override
    public void resumeLoop() {
        paused = false;
    }

    @Override
    public void pauseLoop(LineFilePos lineFile) {
        paused = true;
    }

    public void invalidate() {
        throw new EnvironmentError();
    }

    public boolean isBroken() {
        return broken;
    }
}
