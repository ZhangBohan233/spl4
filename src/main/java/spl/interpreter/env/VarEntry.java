package spl.interpreter.env;

import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;

public class VarEntry {

    private SplElement value;
    public final boolean constant;

    public static VarEntry varEntry(SplElement value) {
        return new VarEntry(value, false);
    }

    public static VarEntry varEntry() {
        return new VarEntry(Undefined.UNDEFINED, false);
    }

    public static VarEntry constEntry(SplElement value) {
        return new VarEntry(value, true);
    }

    public static VarEntry constEntry() {
        return new VarEntry(Undefined.UNDEFINED, true);
    }

    private VarEntry(SplElement value, boolean constant) {
        this.value = value;
        this.constant = constant;
    }

    public SplElement getValue() {
        return value;
    }

    public void setValue(SplElement value) {
        this.value = value;
    }
}
