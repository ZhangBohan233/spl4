package spl.ast;

import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.util.Constants;
import spl.util.LineFile;
import spl.util.Utilities;

public class ThrowStmt extends UnaryExpr {

    public ThrowStmt(LineFile lineFile) {
        super("throw", true, lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement content = value.evaluate(env);
        throwException((Reference) content, env, lineFile);
        return null;
    }

    static void throwException(Reference exceptionClassPtr, Environment env, LineFile lineFile) {
        if (Utilities.isInstancePtr(exceptionClassPtr, Constants.EXCEPTION_CLASS, env, lineFile)) {
            Instance excIns = (Instance) env.getMemory().get((Reference) exceptionClassPtr);

            // the order of code matters.
            // if exception is thrown before 'createString' spl string, spl.interpreter would be stopped.
            // if traceMsg is generated after 'createString' call, stack trace would be changed.
            String traceMsg = makeTraceMsg(env, lineFile);
            Reference tracePtr = StringLiteral.createString(traceMsg.toCharArray(), env, lineFile);
            excIns.getEnv().setVar("traceMsg", tracePtr, lineFile);

            env.throwException(exceptionClassPtr);
        } else {
            SplInvokes.throwException(
                    env,
                    Constants.TYPE_ERROR,
                    "Only classes extends 'Exception' can be thrown.",
                    lineFile);
        }
    }

    private static String makeTraceMsg(Environment env, LineFile lineFile) {
        StringBuilder builder = new StringBuilder();
        builder.append(lineFile.toStringFileLine()).append('\n');
        for (Memory.StackTraceNode stn : env.getMemory().getCallStack()) {
            builder.append("    at ").append(stn.env.definedName).append(". ").append(stn.callLineFile.toStringFileLine()).append('\n');
        }
        return builder.toString();
    }
}
