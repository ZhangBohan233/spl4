package spl.interpreter.env;

import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.HashMap;
import java.util.Map;

public class InstanceEnvironment extends MainAbstractEnvironment {
    private final String className;
    private final Map<String, VarEntry> generics = new HashMap<>();
    private final Map<String, VarEntry> protectedField = new HashMap<>();
    private final Map<String, VarEntry> privateField = new HashMap<>();

    public InstanceEnvironment(String className, Environment definitionEnv) {
        super(definitionEnv.getMemory(), definitionEnv);

        this.className = className;
    }

    /**
     * Define a const and set its value without checking its validity.
     *
     * @param name  name
     * @param value value
     */
    public void directDefineConstAndSet(String name, SplElement value) {
        variables.put(name, VarEntry.constEntry(value));
    }

    @Override
    public void definePrivateConst(String name, LineFilePos lineFilePos) {
        if (localHasName(name, lineFilePos)) {
            throwNameError("Name '" + name + "' is already defined in this scope.", lineFilePos);
            return;
        }
        privateField.put(name, VarEntry.constEntry());
    }

    @Override
    public void definePrivateVar(String name, LineFilePos lineFilePos) {
        if (localHasName(name, lineFilePos)) {
            throwNameError("Name '" + name + "' is already defined in this scope.", lineFilePos);
            return;
        }
        privateField.put(name, VarEntry.varEntry());
    }

    @Override
    public void definePrivateFunction(String name, LineFilePos lineFilePos) {
        super.definePrivateFunction(name, lineFilePos);
    }

    @Override
    public void defineProtectedConst(String name, LineFilePos lineFilePos) {
        if (localHasName(name, lineFilePos)) {
            throwNameError("Name '" + name + "' is already defined in this scope.", lineFilePos);
            return;
        }
        protectedField.put(name, VarEntry.constEntry());
    }

    @Override
    public void defineProtectedVar(String name, LineFilePos lineFilePos) {
        if (localHasName(name, lineFilePos)) {
            throwNameError("Name '" + name + "' is already defined in this scope.", lineFilePos);
            return;
        }
        privateField.put(name, VarEntry.varEntry());
    }

    @Override
    public void defineProtectedFunction(String name, LineFilePos lineFilePos) {
        super.defineProtectedFunction(name, lineFilePos);
    }

    @Override
    public boolean interrupted() {
        return false;
    }

    @Override
    protected VarEntry localInnerGet(String name, LineFilePos lineFile) {
        VarEntry entry = privateField.get(name);
        if (entry != null) return entry;
        entry = protectedField.get(name);
        if (entry != null) return entry;
        return super.localInnerGet(name, lineFile);
    }

    @Override
    protected VarEntry innerGet(String name, boolean isFirst) {
        VarEntry tv = searchSuper(name, true);
        if (tv == null)
            return super.innerGet(name, isFirst);
        else return tv;
    }

    /**
     * @param name    name
     * @param isFirst whether this call is made by spl, not recursively
     * @return the entry, if has
     */
    private VarEntry searchSuper(String name, boolean isFirst) {
        VarEntry instanceEntry = variables.get(Constants.INSTANCE_NAME);
        if (instanceEntry == null) {
            throw new EnvironmentError("Unexpected error: '" + Constants.INSTANCE_NAME + "' not in scope.");
        }
        VarEntry varEntry = generics.get(name);
        if (varEntry != null) {
            return varEntry;
        }

        if (isFirst) {
            varEntry = privateField.get(name);
            if (varEntry != null) return varEntry;
        }
        varEntry = protectedField.get(name);
        if (varEntry != null) return varEntry;

        varEntry = variables.get(name);
        if (varEntry == null) {
            VarEntry superTv = variables.get(Constants.SUPER);
            if (superTv == null) return null;
            else {
                Instance supIns = getMemory().get((Reference) superTv.getValue());
                return supIns.getEnv().searchSuper(name, false);
            }
        } else return varEntry;
    }

    public void defineGeneric(String name, Reference value, LineFilePos lineFilePos) {
        VarEntry varEntry = innerGet(name, true);
        if (varEntry != null) {
            SplInvokes.throwException(
                    this,
                    Constants.NAME_ERROR,
                    "Generic name '" + name + "' already defined in this scope.",
                    lineFilePos
            );
            return;
        }
        generics.put(name, VarEntry.constEntry(value));
    }

    protected VarEntry getGeneric(String name) {  // nullable
        return generics.get(name);
    }

    public Map<String, SplElement> getGenericsMap() {
        Map<String, SplElement> map = new HashMap<>();
        for (Map.Entry<String, VarEntry> entry : generics.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getValue());
        }
        return map;
    }

    @Override
    public String toString() {
        return "InstanceEnv of '" + className + "'";
    }
}
