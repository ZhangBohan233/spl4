package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import interpreter.splObjects.*;
import interpreter.types.*;
import interpreter.primitives.Pointer;
import util.LineFile;

public class Dot extends BinaryExpr  {
    public Dot(LineFile lineFile) {
        super(".", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {

        SplElement leftTv = left.evaluate(env);
        if (!(SplElement.isPrimitive(leftTv))) {
//            PointerType type = (PointerType) leftTv.getType();
            Pointer ptr = (Pointer) leftTv;
            if (ptr.getPtr() == 0) {
                throw new SplException("Pointer to null does not support attributes operation. ",
                        getLineFile());
            }
            return null;
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
//            if(!(funcTv.getType() instanceof CallableType))
//                throw new SplException("Class attribute not callable. ", lineFile);
            SplCallable callable = (SplCallable) objEnv.getMemory().get((Pointer) funcTv);
            return callable.call(((FuncCall) right).getArguments(), oldEnv);
        } else {
            throw new SplException("Unexpected right side type of dot '" + right.getClass() + "' ", lineFile);
        }
    }

}
