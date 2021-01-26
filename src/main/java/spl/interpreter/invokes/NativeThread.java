package spl.interpreter.invokes;

import spl.ast.Arguments;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.env.ThreadEnvironment;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Function;
import spl.interpreter.splObjects.NativeObject;
import spl.util.Accessible;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Utilities;

public class NativeThread extends NativeObject {

    private final Function target;
    private final Reference object;
    private final Environment callingEnv;
    private final LineFilePos callingLf;
    private final RealThread realThread;
    private int threadId = -1;

    public NativeThread(Function target, Reference object, Environment callingEnv, boolean daemonic,
                        LineFilePos callingLf) {
        this.target = target;
        this.object = object;
        this.callingEnv = callingEnv;
        this.callingLf = callingLf;

        this.realThread = new RealThread();
        realThread.setDaemon(daemonic);
    }

    @Accessible
    public Undefined start(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 0, "NativeThread.start", env, lineFilePos);

        realThread.start();

        return Undefined.UNDEFINED;
    }

    @Accessible
    public Undefined interrupt(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 0, "NativeThread.interrupt", env, lineFilePos);

        realThread.interrupt();

        return Undefined.UNDEFINED;
    }

    @Accessible
    public Int threadId(Arguments arguments, Environment env, LineFilePos lineFilePos) {
        checkArgCount(arguments, 0, "NativeThread.threadId", env, lineFilePos);

        return new Int(threadId);
    }

    private class RealThread extends Thread {
        @Override
        public void run() {
            threadId = callingEnv.getMemory().newThread();
            ThreadEnvironment threadEnv = new ThreadEnvironment(callingEnv, threadId);
            try {
                target.call(EvaluatedArguments.of(object), threadEnv, callingLf);
                if (threadEnv.hasException()) {
                    Utilities.removeErrorAndPrint(threadEnv, callingLf);
                }
            } finally {
                callingEnv.getMemory().endThread(threadId);
            }
        }
    }
}
