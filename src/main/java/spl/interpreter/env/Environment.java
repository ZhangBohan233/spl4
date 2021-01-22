package spl.interpreter.env;

import spl.interpreter.Memory;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Environment {

    private static int envCount = 0;
    public final Environment outer;
    public final GlobalEnvironment globalEnv;
    /**
     * Id is guaranteed unique.
     */
    private final int envId;
    protected Memory memory;

    protected Map<String, VarEntry> variables = new HashMap<>();

    public Environment(Memory memory, Environment outer) {
        this.memory = memory;
        this.outer = outer;
        this.globalEnv = outer == null ? (GlobalEnvironment) this : outer.globalEnv;
        this.envId = envCount++;
    }

    public Memory getMemory() {
        return memory;
    }

    public void defineFunction(String name, Reference funcPtr, LineFilePos lineFile) {
        variables.put(name, VarEntry.varEntry(funcPtr));
    }

    public Reference getImportedModulePtr(String modulePath) {
        return outer.getImportedModulePtr(modulePath);
    }

    public void addImportedModulePtr(String modulePath, Reference modulePtr) {
        outer.addImportedModulePtr(modulePath, modulePtr);
    }

    public abstract boolean isSub();

    public abstract void setReturn(SplElement typeValue, LineFilePos lineFile);

    public abstract boolean interrupted();

    public abstract void breakLoop(LineFilePos lineFile);

    public abstract void resumeLoop();

    public abstract void pauseLoop(LineFilePos lineFile);

    public abstract void invalidate();

    public abstract void fallthrough(LineFilePos lineFile);

    public abstract boolean isFallingThrough();

    public abstract void yield(SplElement value, LineFilePos lineFile);

    public abstract SplElement yieldResult();

    public void throwException(Reference exceptionPtr) {
        outer.throwException(exceptionPtr);
    }

    public boolean hasException() {
        return globalEnv.hasException();
    }

    public Set<SplElement> attributes() {
        Set<SplElement> set = new HashSet<>();
        for (VarEntry entry : variables.values()) {
            set.add(entry.getValue());
        }
        return set;
    }

    public Map<String, SplElement> keyAttributes() {
        Map<String, SplElement> attrs = new HashMap<>();
        for (Map.Entry<String, VarEntry> entry : variables.entrySet()) {
            attrs.put(entry.getKey(), entry.getValue().getValue());
        }
        return attrs;
    }

    public Set<String> names() {
        return new HashSet<>(variables.keySet());
    }

    public void defineVar(String name, LineFilePos lineFile) {
        if (localHasName(name, lineFile)) {
            throwNameError("Variable '" + name + "' already defined.", lineFile);
            return;
        }

        variables.put(name, VarEntry.varEntry());
    }

    public void defineVarAndSet(String name, SplElement value, LineFilePos lineFile) {
        if (localHasName(name, lineFile)) {
            throwNameError("Variable '" + name + "' already defined.", lineFile);
            return;
        }

        variables.put(name, VarEntry.varEntry(value));
    }

    public void defineConst(String name, LineFilePos lineFile) {
        if (localHasName(name, lineFile)) {
            throwNameError("Constant '" + name + "' already defined.", lineFile);
            return;
        }

        // not using 'defaultValue' because 'null' is the mark of unassigned constant
        variables.put(name, VarEntry.constEntry());
    }

    public void defineConstAndSet(String name, SplElement value, LineFilePos lineFile) {
        if (localHasName(name, lineFile)) {
            throwNameError("Constant '" + name + "' already defined.", lineFile);
            return;
        }

        variables.put(name, VarEntry.constEntry(value));
    }

    public void setVar(String name, SplElement value, LineFilePos lineFile) {
        VarEntry entry = innerGet(name, true);
        if (entry == null) {
            throwNameError("Variable '" + name + "' is not defined in this scope.", lineFile);
            return;
        }

        if (entry.constant && entry.getValue() != Undefined.UNDEFINED) {
            throwNameError("Constant '" + name + "' is not assignable.", lineFile);
            return;
        }

        entry.setValue(value);
    }

    public SplElement get(String name, LineFilePos lineFile) {
        VarEntry se = innerGet(name, true);
        if (se == null) {
            throwNameError("Name '" + name + "' not found.", lineFile);
            return Undefined.ERROR;
        }

        return se.getValue();
    }

    public boolean hasName(String name) {
        return innerGet(name, true) != null;
    }

    /**
     * Get a value stored by name.
     * <p>
     * Note that this method is overridden in {@code InstanceEnvironment}
     *
     * @param name    the name
     * @param isFirst whether this is called by another function. {@code false} if this call is self recursion
     * @return the value
     */
    protected VarEntry innerGet(String name, boolean isFirst) {
        VarEntry tv = variables.get(name);

        if (tv == null) {
            if (outer != null) {
                tv = outer.innerGet(name, false);
            }
            if (isFirst && tv == null) {
                tv = searchInNamespaces(name);
            }
        }
        return tv;
    }

    protected VarEntry localInnerGet(String name, LineFilePos lineFile) {
        VarEntry tv = variables.get(name);
        if (tv == null) {
            if (outer != null && outer.isSub()) {
                tv = outer.localInnerGet(name, lineFile);
            }
        }
        return tv;
    }

    protected final boolean localHasName(String name, LineFilePos lineFile) {
        return localInnerGet(name, lineFile) != null;
    }

    public abstract void addNamespace(ModuleEnvironment moduleEnvironment);

    protected abstract VarEntry searchInNamespaces(String name);

    protected abstract void setInNamespaces(String name, SplElement typeValue);

    protected void throwNameError(String msg, LineFilePos lineFilePos) {
        SplInvokes.throwException(
                this,
                Constants.NAME_ERROR,
                msg,
                lineFilePos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Environment that = (Environment) o;

        return envId == that.envId;
    }

    @Override
    public int hashCode() {
        return envId;
    }
}
