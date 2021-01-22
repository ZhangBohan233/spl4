package spl.interpreter.invokes;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splObjects.Function;
import spl.util.LineFilePos;

public class NativeThread extends Thread {

    private final Function target;
    private final Reference object;
    private final Environment callingEnv;
    private final LineFilePos callingLf;

    public NativeThread(Function target, Reference object, Environment callingEnv, boolean daemonic,
                        LineFilePos callingLf) {
        this.target = target;
        this.object = object;
        this.callingEnv = callingEnv;
        this.callingLf = callingLf;

        setDaemon(daemonic);
    }

    @Override
    public void run() {
        target.call(EvaluatedArguments.of(object), callingEnv, callingLf);
    }
}
