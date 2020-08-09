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
                return crossEnvEval(right, ((Instance) leftObj).getEnv(), env, getLineFile());
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
//            switch (type.getPointerType()) {
//                case PointerType.CLASS_TYPE:
//                    Instance instance = (Instance) env.getMemory().get(ptr);
//                    return crossEnvEval(right, instance.getEnv(), env, getLineFile());
//                case PointerType.MODULE_TYPE:
//                    SplModule module = (SplModule) env.getMemory().get(ptr);
//                    return crossEnvEval(right, module.getEnv(), env, getLineFile());
////                    return right.evaluate(module.getEnv());
//                case PointerType.ARRAY_TYPE:
//                    SplArray arr = (SplArray) env.getMemory().get(ptr);
//                    if (arr == null) {
//                        System.out.println(ptr);
//                        System.out.println(left);
//                        throw new NullPointerException("Array is null. ");
//                    }
//                    return arr.getAttr(right, getLineFile());
//                case PointerType.NATIVE_TYPE:
//                    NativeObject nativeObject = (NativeObject) env.getMemory().get(ptr);
//                    return nativeObject.invoke(right, env, getLineFile());
//                default:
//                    throw new TypeError("Type '" + type + "' does not support attributes operation. ",
//                            getLineFile());
//            }
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
