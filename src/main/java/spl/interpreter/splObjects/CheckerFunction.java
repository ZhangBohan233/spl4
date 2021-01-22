package spl.interpreter.splObjects;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.Accessible;

public class CheckerFunction extends NativeFunction {

    @Accessible
    public final Reference __class__;

    public CheckerFunction(String name, Reference clazzPtr) {
        super(name, 1);

        this.__class__ = clazzPtr;
    }

    @Override
    protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv) {
        SplElement arg = evaluatedArgs.positionalArgs.get(0);
        if (arg instanceof Reference) {
            SplObject obj = callingEnv.getMemory().get((Reference) arg);
            if (obj instanceof Instance) {
                Reference argClazzPtr = ((Instance) obj).getClazzPtr();
                return Bool.boolValueOf(
                        SplClass.isSuperclassOf(
                                __class__,
                                argClazzPtr,
                                callingEnv.getMemory()));
            }
        }
        return Bool.FALSE;
    }
}
