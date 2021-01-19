package spl.interpreter.env;

import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplClass;
import spl.util.Constants;
import spl.util.LineFilePos;

public class MethodEnvironment extends FunctionEnvironment {

    private final int methodDefClassId;

    public MethodEnvironment(Environment definitionEnv,
                             Environment callingEnv,
                             String definedName,
                             int methodDefClassId) {
        super(definitionEnv, callingEnv, definedName);

        this.methodDefClassId = methodDefClassId;
    }

    private VarEntry searchGenerics(String name, Instance someIns) {
        VarEntry instanceEntry = getInstanceEntry(someIns);
        if (instanceEntry == null) return null;
        Instance targetIns = getMemory().get((Reference) instanceEntry.getValue());
        return targetIns.getEnv().getGeneric(name);
    }

    private VarEntry getInstanceEntry(Instance someIns) {
        SplClass thisClass = getMemory().get(someIns.getClazzPtr());
        if (thisClass.classId == methodDefClassId) {
            return someIns.getEnv().innerGet(Constants.INSTANCE_NAME, false);
        } else {
            VarEntry superTv = someIns.getEnv().innerGet(Constants.SUPER, false);
            if (superTv == null) return null;
            else {
                Instance supIns = getMemory().get((Reference) superTv.getValue());
                return getInstanceEntry(supIns);
            }
        }
    }

    @Override
    protected VarEntry innerGet(String name, boolean isFirst) {
        VarEntry thisEntry = variables.get(Constants.THIS);
        if (thisEntry == null) { // do not remove this
            throw new EnvironmentError("Unexpected error: '" + Constants.THIS + "' not in scope. ",
                    LineFilePos.LF_INTERPRETER);
        }
        SplElement value = thisEntry.getValue();
        if (value == Undefined.ERROR) return null;
        if (value == Undefined.UNDEFINED) {
            VarEntry ve = variables.get(name);
            if (ve == null) return super.innerGet(name, isFirst);
            else return ve;
        }

        Instance thisIns = getMemory().get((Reference) thisEntry.getValue());
        if (name.equals(Constants.INSTANCE_NAME)) return getInstanceEntry(thisIns);

        VarEntry entry = searchGenerics(name, thisIns);
        if (entry != null) return entry;
        entry = variables.get(name);
        if (entry == null) {
            entry = thisIns.getEnv().innerGet(name, isFirst);
            if (entry != null) return entry;
        }

        return super.innerGet(name, isFirst);
    }
}
