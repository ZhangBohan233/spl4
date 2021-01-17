package spl.interpreter.env;

import spl.interpreter.primitives.Reference;
import spl.interpreter.splObjects.Instance;
import spl.util.Constants;
import spl.util.LineFilePos;

public class MethodEnvironment extends FunctionEnvironment {

    public MethodEnvironment(Environment definitionEnv, Environment callingEnv, String definedName) {
        super(definitionEnv, callingEnv, definedName);
    }

    @Override
    protected VarEntry innerGet(String name, boolean isFirst) {
        VarEntry entry = variables.get(name);
        if (entry == null) {
            VarEntry thisEntry = variables.get(Constants.THIS);
//            if (thisEntry == null) { // do not remove this
//                throw new EnvironmentError("Pointer 'this' not in scope. ", LineFilePos.LF_INTERPRETER);
//            }
            if (thisEntry != null) {
                Instance thisIns = getMemory().get((Reference) thisEntry.getValue());
                entry = thisIns.getEnv().innerGet(name, isFirst);
                if (entry != null) return entry;
            }
        }

        return super.innerGet(name, isFirst);
    }
}
