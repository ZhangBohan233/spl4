package spl.interpreter.splObjects;

public class NativeType extends SplObject {

    private final String typeName;

    public NativeType(String typeName) {
        this.typeName = typeName;
    }

    public static String shownName(String typeName) {
        return "NativeType_" + typeName;
    }

    @Override
    public String toString() {
        return shownName(typeName);
    }
}
