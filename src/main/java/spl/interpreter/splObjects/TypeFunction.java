package spl.interpreter.splObjects;

public abstract class TypeFunction extends NativeFunction {
    public TypeFunction(String name, int leastArg, int mostArg) {
        super(name, leastArg, mostArg);
    }

    public TypeFunction(String name, int argCount) {
        super(name, argCount);
    }
}
