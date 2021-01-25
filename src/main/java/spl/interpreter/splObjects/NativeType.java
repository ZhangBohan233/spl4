package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;
import spl.util.Utilities;

public class NativeType extends NativeObject implements ClassLike {

    private final String typeName;
    private final Class<? extends NativeObject> clazz;

    private Reference checkerFn;

    public NativeType(String typeName, Class<? extends NativeObject> clazz) {
        this.typeName = typeName;
        this.clazz = clazz;
    }

    public static String shownName(String typeName) {
        return "NativeType_" + typeName;
    }

    public void setCheckerFn(Reference checkerFn) {
        this.checkerFn = checkerFn;
    }

    @Override
    public String toString() {
        return shownName(typeName);
    }

    public Class<? extends NativeObject> getClazz() {
        return clazz;
    }

    @Override
    public Bool __superclassOf__(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 1, "NativeType.__superclassOf__", env, lineFilePos);

        SplElement arg = args.getLine().get(0).evaluate(env);
        if (arg instanceof Reference) {
            SplObject obj = env.getMemory().get((Reference) arg);
            if (obj instanceof NativeType) {
                return Bool.boolValueOf(Utilities.superclassOf(clazz, ((NativeType) obj).clazz));
            }
        }
        return Bool.FALSE;
    }

    @Override
    public Reference __checker__(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "TypeFunction.__checker__", env, lineFilePos);

        return checkerFn;
    }
}
