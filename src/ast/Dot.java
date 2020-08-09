package ast;

import interpreter.env.InstanceEnvironment;
import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import interpreter.splErrors.TypeError;
import interpreter.splObjects.*;
import interpreter.primitives.Pointer;
import util.LineFile;

public class Dot extends BinaryExpr {
    public Dot(LineFile lineFile) {
        super(".", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {

        SplElement leftTv = left.evaluate(env);
        if (leftTv instanceof Pointer) {
//            PointerType type = (PointerType) leftTv.getType();
            Pointer ptr = (Pointer) leftTv;
            if (ptr.getPtr() == 0) {
                throw new NativeError("Pointer to null does not support attributes operation. ",
                        getLineFile());
            }
            SplObject leftObj = env.getMemory().get(ptr);
            if (leftObj instanceof Instance) {
                env.getMemory().setCurrentThisPtr(ptr);
                SplElement ele = crossEnvEval(right, ((Instance) leftObj).getEnv(), env, getLineFile());
                env.getMemory().removeThisPtr();
                return ele;
            } else if (leftObj instanceof SplModule) {
                return crossEnvEval(right, ((SplModule) leftObj).getEnv(), env, getLineFile());
            } else if (leftObj instanceof SplArray) {
                return ((SplArray) leftObj).getAttr(right, getLineFile());
            } else if (leftObj instanceof NativeObject) {
                return ((NativeObject) leftObj).invoke(right, env, getLineFile());
            } else if (leftObj instanceof SplClass) {
                return ((SplClass) leftObj).getAttr(ptr, right, env, lineFile);
            } else {
                throw new TypeError("Object '" + leftObj + "' does not support attributes operation. ",
                        getLineFile());
            }
        } else {
            throw new TypeError("Only pointer type supports attributes operation. ", getLineFile());
        }
    }

    private static SplElement crossEnvEval(Node right, Environment objEnv, Environment oldEnv, LineFile lineFile) {
        if (right instanceof NameNode) {
            return right.evaluate(objEnv);
        } else if (right instanceof FuncCall) {
            SplElement funcTv = ((FuncCall) right).getCallObj().evaluate(objEnv);
            SplCallable callable = (SplCallable) objEnv.getMemory().get((Pointer) funcTv);
            Arguments arg = ((FuncCall) right).getArguments();
            if (callable instanceof Method) {
                return ((Method) callable).methodCall(arg.evalArgs(oldEnv),
                        oldEnv,
                        (InstanceEnvironment) objEnv,
                        lineFile);
            } else {
                return callable.call(arg, oldEnv);
            }
        } else if (right instanceof IndexingNode) {
            return ((IndexingNode) right).crossEnvEval(objEnv, oldEnv);
        } else {
            throw new NativeError("Unexpected right side type of dot '" + right.getClass() + "' ", lineFile);
        }
    }

}
