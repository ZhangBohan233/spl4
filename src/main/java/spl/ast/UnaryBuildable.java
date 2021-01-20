package spl.ast;

public interface UnaryBuildable extends Buildable {

    void setValue(Expression value);

    Expression getValue();

    boolean operatorAtLeft();

    boolean voidAble();
}
