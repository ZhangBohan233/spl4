package spl.interpreter.env;

import spl.interpreter.*;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.util.Constants;
import spl.util.LineFile;

import java.util.*;

public abstract class Environment {

    private static int envCount = 0;

    /**
     * Id is guaranteed unique.
     */
    private final int envId;

    public final Environment outer;
    public final GlobalEnvironment globalEnv;
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

    public void defineFunction(String name, Reference funcPtr, LineFile lineFile) {
        variables.put(name, VarEntry.varEntry(funcPtr));
    }

    public Reference getImportedModulePtr(String modulePath) {
        return outer.getImportedModulePtr(modulePath);
    }

    public void addImportedModulePtr(String modulePath, Reference modulePtr) {
        outer.addImportedModulePtr(modulePath, modulePtr);
    }

    public abstract boolean isSub();

    public abstract void setReturn(SplElement typeValue);

    public abstract boolean interrupted();

    public abstract void breakLoop();

    public abstract void resumeLoop();

    public abstract void pauseLoop();

    public abstract void invalidate();

    public abstract void fallthrough(LineFile lineFile);

    public abstract boolean isFallingThrough();

    public abstract void yield(SplElement value, LineFile lineFile);

    public abstract SplElement yieldResult();

    public void throwException(Reference exceptionPtr) {
        outer.throwException(exceptionPtr);
    }

    public boolean hasException() {
        return globalEnv.hasException();
    }

    public Set<SplElement> attributes() {
        Set<SplElement> set = new HashSet<>();
//        set.addAll(constants.values());
//        set.addAll(variables.values());
        for (VarEntry entry : variables.values()) {
            set.add(entry.getValue());
        }

        return set;
    }

    public void defineVar(String name, LineFile lineFile) {
        if (localHasName(name, lineFile))
            throw new EnvironmentError("Variable '" + name + "' already defined. ", lineFile);

        variables.put(name, VarEntry.varEntry());
    }

    public void defineVarAndSet(String name, SplElement value, LineFile lineFile) {
        if (localHasName(name, lineFile))
            throw new EnvironmentError("Variable '" + name + "' already defined. ", lineFile);

        variables.put(name, VarEntry.varEntry(value));
    }

    public void defineConst(String name, LineFile lineFile) {
        if (localHasName(name, lineFile))
            throw new EnvironmentError("Constant '" + name + "' already defined. ", lineFile);

        // not using 'defaultValue' because 'null' is the mark of unassigned constant
        variables.put(name, VarEntry.constEntry());
    }

    public void defineConstAndSet(String name, SplElement value, LineFile lineFile) {
        if (localHasName(name, lineFile))
            throw new EnvironmentError("Constant '" + name + "' already defined. ", lineFile);

        variables.put(name, VarEntry.constEntry(value));
    }

    public void setVar(String name, SplElement value, LineFile lineFile) {
        VarEntry entry = innerGet(name, true);
        if (entry == null)
            throw new EnvironmentError("Variable '" + name + "' is not defined in this scope. ", lineFile);

        if (entry.constant && entry.getValue() != Undefined.UNDEFINED) {
            throw new EnvironmentError("Constant '" + name + "' is not assignable. ", lineFile);
        }

        entry.setValue(value);
    }

    public SplElement get(String name, LineFile lineFile) {
        VarEntry se = innerGet(name, true);
        if (se == null) {
//            throw new EnvironmentError("Name '" + name + "' not found. ", lineFile);
            SplInvokes.throwException(this, Constants.NAME_ERROR, "Name '" + name + "' not found. ", lineFile);
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
     * @param name         the name
     * @param isFirst      whether this is called by another function. {@code false} if this call is self recursion
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

    protected VarEntry localInnerGet(String name, LineFile lineFile) {
        VarEntry tv = variables.get(name);
        if (tv == null) {
            if (outer != null && outer.isSub()) {
                tv = outer.localInnerGet(name, lineFile);
            }
        }
        return tv;
    }

    protected final boolean localHasName(String name, LineFile lineFile) {
        return localInnerGet(name, lineFile) != null;
    }

    public void printVars() {
        System.out.println(variables);
    }

//    private boolean typeCheck(Type inStock, Type assignment) {
//        return inStock.isSuperclassOfOrEquals(assignment, this);
//    }

    public abstract void addNamespace(ModuleEnvironment moduleEnvironment);

    protected abstract VarEntry searchInNamespaces(String name);

    protected abstract void setInNamespaces(String name, SplElement typeValue);

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
