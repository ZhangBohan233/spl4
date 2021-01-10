package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.*;
import spl.util.Constants;
import spl.util.LineFilePos;

public class Dot extends BinaryExpr {
    public Dot(LineFilePos lineFile) {
        super(".", lineFile);
    }

    private static SplElement crossEnvEval(Node right, Reference leftPtr,
                                           Environment objEnv, Environment oldEnv, LineFilePos lineFile) {
        if (right instanceof NameNode) {
            return right.evaluate(objEnv);
        } else if (right instanceof FuncCall) {
            SplElement funcTv = ((FuncCall) right).getCallObj().evaluate(objEnv);
            if (objEnv.hasException()) {
                return Undefined.ERROR;
            }
            SplCallable callable = (SplCallable) objEnv.getMemory().get((Reference) funcTv);
            EvaluatedArguments ea = ((FuncCall) right).getArguments().evalArgs(oldEnv);
            if (callable instanceof SplMethod) {
                ea.insertThis(leftPtr);  // add "this" ptr
            }
            return callable.call(ea, oldEnv, lineFile);
        } else if (right instanceof IndexingNode) {
            return ((IndexingNode) right).crossEnvEval(objEnv, oldEnv);
        } else {
            return SplInvokes.throwExceptionWithError(
                    oldEnv,
                    Constants.TYPE_ERROR,
                    "Unexpected right side type of dot '" + right.getClass() + "'",
                    lineFile);
        }
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement leftTv = left.evaluate(env);
        if (env.hasException()) return Undefined.ERROR;
        if (leftTv instanceof Reference) {
            Reference ptr = (Reference) leftTv;
            if (ptr.getPtr() == 0) {
                return SplInvokes.throwExceptionWithError(
                        env,
                        Constants.TYPE_ERROR,
                        "Pointer to null does not support attributes operation.",
                        lineFile);
            }
            SplObject leftObj = env.getMemory().get(ptr);
            if (leftObj instanceof Instance) {
                return crossEnvEval(right, ptr, ((Instance) leftObj).getEnv(), env, getLineFile());
            } else if (leftObj instanceof SplModule) {
                return crossEnvEval(right, ptr, ((SplModule) leftObj).getEnv(), env, getLineFile());
            } else if (leftObj instanceof SplArray) {
                return ((SplArray) leftObj).getAttr(right, env, getLineFile());
            } else if (leftObj instanceof NativeObject) {
                return ((NativeObject) leftObj).invoke(right, env, getLineFile());
            } else if (leftObj instanceof SplClass) {
                return ((SplClass) leftObj).getAttr(ptr, right, env, lineFile);
            } else {
                return SplInvokes.throwExceptionWithError(
                        env,
                        Constants.TYPE_ERROR,
                        "Object '" + leftObj + "' does not support attributes operation.",
                        lineFile);
            }
        } else {
            return SplInvokes.throwExceptionWithError(
                    env,
                    Constants.TYPE_ERROR,
                    "Only pointer type supports attributes operation.",
                    lineFile);
        }
    }

}
