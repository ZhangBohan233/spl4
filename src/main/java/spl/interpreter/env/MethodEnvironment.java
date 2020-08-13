package spl.interpreter.env;

import spl.interpreter.primitives.Pointer;
import spl.interpreter.splObjects.Instance;
import spl.util.Constants;
import spl.util.LineFile;

public class MethodEnvironment extends FunctionEnvironment {

    public MethodEnvironment(Environment definitionEnv, Environment callingEnv, String definedName) {
        super(definitionEnv, callingEnv, definedName);
    }

    @Override
    protected VarEntry innerGet(String name, boolean isFirst) {
        VarEntry entry = variables.get(name);
        if (entry == null) {
            VarEntry thisEntry = variables.get(Constants.THIS);
            if (thisEntry == null)
                throw new EnvironmentError("Pointer 'this' not in scope. ", LineFile.LF_INTERPRETER);
            Instance thisIns = (Instance) getMemory().get((Pointer) thisEntry.getValue());
            entry = thisIns.getEnv().innerGet(name, isFirst);
            if (entry != null) return entry;
        }

        return super.innerGet(name, isFirst);
    }
}
