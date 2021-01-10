package spl.interpreter.splObjects;

public class NativeType extends SplObject {

    private final String typeName;

    public NativeType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
