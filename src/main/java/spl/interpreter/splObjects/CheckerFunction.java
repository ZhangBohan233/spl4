package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.Accessible;
import spl.util.LineFilePos;

public class CheckerFunction extends NativeFunction {

    public final Reference typeClass;

    public CheckerFunction(String name, Reference clazzPtr) {
        super(name, 1);

        this.typeClass = clazzPtr;
    }

    @Override
    protected Bool callFunc(EvaluatedArguments evaluatedArgs, Environment callingEnv,
                            LineFilePos callingLfp) {
        SplElement arg = evaluatedArgs.positionalArgs.get(0);
        if (arg instanceof Reference) {
            SplObject obj = callingEnv.getMemory().get((Reference) arg);
            if (obj instanceof Instance) {
                Reference argClazzPtr = ((Instance) obj).getClazzPtr();
                return Bool.boolValueOf(
                        SplClass.isSuperclassOf(
                                typeClass,
                                argClazzPtr,
                                callingEnv.getMemory()));
            }
        }
        return Bool.FALSE;
    }

    @Accessible
    public SplElement __class__(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "CheckerFunction.__class__", env, lineFilePos);

        return typeClass;
    }
}
