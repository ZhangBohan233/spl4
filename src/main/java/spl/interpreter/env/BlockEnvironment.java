package spl.interpreter.env;


import spl.util.LineFile;

public class BlockEnvironment extends SubAbstractEnvironment {

    public BlockEnvironment(Environment outer) {
        super(outer);
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

    public void invalidate() {
        variables.clear();
    }
}
