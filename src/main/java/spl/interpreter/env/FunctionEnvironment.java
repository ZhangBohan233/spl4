package spl.interpreter.env;

import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

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
        return Objects.requireNonNullElse(returnValue, Reference.NULL);
    }

    /**
     * Resume this environment after returning.
     * <p>
     * This method is used in two cases:
     * 1. Checking return contract.
     * 2. The 'finally' block in try-catch-finally statement, when a return statement is
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
    public void setReturn(SplElement typeValue, LineFilePos lineFile) {
        returnValue = typeValue;
    }

    @Override
    public void throwException(Reference exceptionPtr) {
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
