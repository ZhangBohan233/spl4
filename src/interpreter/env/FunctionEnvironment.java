package interpreter.env;

import interpreter.primitives.SplElement;

public class FunctionEnvironment extends MainAbstractEnvironment {

    public final Environment callingEnv;
    public final String definedName;
    private SplElement returnValue;

    public FunctionEnvironment(Environment definitionEnv, Environment callingEnv, String definedName) {
        super(definitionEnv.memory, definitionEnv);

        this.callingEnv = callingEnv;
        this.definedName = definedName;
    }

    public SplElement getReturnValue() {
        return returnValue;
    }

    @Override
    public void setReturn(SplElement typeValue) {
        returnValue = typeValue;
    }

    @Override
    public boolean interrupted() {
        return returnValue != null;
    }

    @Override
    public String toString() {
        return "FunctionEnv '" + definedName + "'";
    }
}
