package ast;

import interpreter.Memory;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Instance;
import interpreter.splErrors.TypeError;
import util.Constants;
import util.LineFile;
import util.Utilities;

public class ThrowStmt extends UnaryExpr {

    public ThrowStmt(LineFile lineFile) {
        super("throw", true, lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement content = value.evaluate(env);
        throwException((Pointer) content, env, lineFile);
        return null;
    }

    static void throwException(Pointer exceptionClassPtr, Environment env, LineFile lineFile) {
        if (Utilities.isInstancePtr(exceptionClassPtr, Constants.EXCEPTION_CLASS, env, lineFile)) {
            Instance excIns = (Instance) env.getMemory().get((Pointer) exceptionClassPtr);

            // the order of code matters.
            // if exception is thrown before 'createString' spl string, interpreter would be stopped.
            // if traceMsg is generated after 'createString' call, stack trace would be changed.
            String traceMsg = makeTraceMsg(env, lineFile);
            Pointer tracePtr = StringLiteral.createString(traceMsg.toCharArray(), env, lineFile);
            excIns.getEnv().setVar("traceMsg", tracePtr, lineFile);

            env.throwException((Pointer) exceptionClassPtr);
        } else {
            throw new TypeError("Only classes extends 'Exception' can be thrown. ", lineFile);
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
