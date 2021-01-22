package spl.interpreter.splObjects;

import spl.interpreter.primitives.Reference;
import spl.util.Accessible;

public abstract class TypeFunction extends NativeFunction {

    @Accessible
    public Reference __checker__ = Reference.NULL;

    public TypeFunction(String name) {
        super(name, 1);
    }

    public void setChecker(Reference __checker__) {
        this.__checker__ = __checker__;
    }
}
