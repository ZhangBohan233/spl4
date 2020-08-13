package spl.interpreter.env;

import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;

import java.util.Objects;

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
        return Objects.requireNonNullElse(returnValue, Pointer.NULL_PTR);
    }

    /**
     * Resume this environment after returning.
     * <p>
     * This method is only used for 'finally' block in try-catch-finally statement, when a return statement is
     * executed in the 'try' or 'catch' block.
     *
     * @return the previous return value, nullable
     */
    public SplElement temporaryRemoveRtn() {
        SplElement v = returnValue;
        returnValue = null;
        return v;
    }

    @Override
    public void setReturn(SplElement typeValue) {
        returnValue = typeValue;
    }

    @Override
    public void throwException(Pointer exceptionPtr) {
        callingEnv.throwException(exceptionPtr);
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
