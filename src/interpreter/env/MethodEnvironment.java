package interpreter.env;

import interpreter.primitives.Pointer;
import interpreter.splObjects.Instance;
import util.Constants;
import util.LineFile;

public class MethodEnvironment extends FunctionEnvironment {

    public MethodEnvironment(Environment definitionEnv, Environment callingEnv, String definedName) {
        super(definitionEnv, callingEnv, definedName);
    }

    @Override
    protected VarEntry innerGet(String name, boolean isFirst, LineFile lineFile) {
        VarEntry entry = variables.get(name);
        Instance thisIns = null;
        if (entry == null) {
            VarEntry thisVar = variables.get(Constants.THIS);
            Pointer thisPtr = (Pointer) thisVar.getValue();
            if (thisPtr == null) throw new EnvironmentError("Method not in instance. ", lineFile);
            thisIns = (Instance) getMemory().get(thisPtr);
            entry = thisIns.getEnv().innerGet(name, false, lineFile);
            if (entry != null) {
//                System.out.println(name);
                return entry;
            }
        }
        entry = super.innerGet(name, isFirst, lineFile);
//        if (entry == null) {
//            System.out.println(thisIns);
//        }
        return entry;
    }
}
