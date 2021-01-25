package spl.interpreter.splObjects;

import spl.ast.Arguments;
import spl.interpreter.Memory;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.Accessible;
import spl.util.LineFilePos;

public abstract class TypeFunction extends NativeFunction implements ClassLike {

    public Reference checkerFn = Reference.NULL;

    public TypeFunction(String name) {
        super(name, 1);
    }

    public void setChecker(Reference checkerFn) {
        this.checkerFn = checkerFn;
    }

    @Accessible
    public Reference __checker__(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "TypeFunction.__checker__", env, lineFilePos);

        return checkerFn;
    }

    @Accessible
    public Bool __superclassOf__(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 1, "TypeFunction.__superclassOf__", env, lineFilePos);

        SplElement arg = args.getLine().get(0).evaluate(env);
        if (arg instanceof Reference) {
            Reference ref = (Reference) arg;
            SplObject obj = env.getMemory().get(ref);
            if (obj instanceof ClassLike) {
                return Bool.boolValueOf(isSuperOf(ref, (ClassLike) obj));
            }
        }
        return Bool.FALSE;
    }

    public abstract boolean isSuperOf(Reference otherPtr, ClassLike other);

    /**
     * Primitive type function.
     */
    public abstract static class Primitive extends TypeFunction {

        public Primitive(String name) {
            super(name);
        }

        @Override
        public boolean isSuperOf(Reference otherPtr, ClassLike other) {
            return other instanceof TypeFunction && getName().equals(((TypeFunction) other).getName());
        }
    }
}
