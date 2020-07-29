package ast;

public interface UnaryBuildable extends Buildable {

    void setValue(Node value);

    boolean operatorAtLeft();

    boolean voidAble();
}
