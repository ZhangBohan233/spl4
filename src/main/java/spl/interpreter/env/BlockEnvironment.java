package spl.interpreter.env;


import spl.util.LineFilePos;

public class BlockEnvironment extends SubAbstractEnvironment {

    public BlockEnvironment(Environment outer) {
        super(outer);
    }

    @Override
    public boolean interrupted() {
        return outer.interrupted();
    }

    @Override
    public void breakLoop(LineFilePos lineFile) {
        outer.breakLoop(lineFile);
    }

    @Override
    public void resumeLoop() {
        outer.resumeLoop();
    }

    @Override
    public void pauseLoop(LineFilePos lineFile) {
        outer.pauseLoop(lineFile);
    }

    public void invalidate() {
        variables.clear();
    }
}
