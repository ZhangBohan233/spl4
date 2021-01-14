package spl.ast;

public interface UnaryBuildable extends Buildable {

    void setValue(Expression value);

    boolean operatorAtLeft();

    boolean voidAble();
}
