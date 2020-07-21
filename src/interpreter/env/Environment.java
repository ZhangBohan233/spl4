package interpreter.env;

import interpreter.*;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.primitives.Undefined;
import util.LineFile;

import java.io.File;
import java.util.*;

public abstract class Environment {

    private static int envCount = 0;

    /**
     * Id is guaranteed unique.
     */
    private final int envId;

    public final Environment outer;
    protected Memory memory;

    protected Map<String, VarEntry> variables = new HashMap<>();
//    protected Map<String, VarEntry> constants = new HashMap<>();

    public Environment(Memory memory, Environment outer) {
        this.memory = memory;
        this.outer = outer;
        this.envId = envCount++;
    }

    public Memory getMemory() {
        return memory;
    }

    public void defineFunction(String name, Pointer funcPtr, LineFile lineFile) {
        defineVarAndSet(name, funcPtr, lineFile);
    }

    public Pointer getImportedModulePtr(File moduleFile) {
        return outer.getImportedModulePtr(moduleFile);
    }

    public void addImportedModulePtr(File moduleFile, Pointer modulePtr) {
        outer.addImportedModulePtr(moduleFile, modulePtr);
    }

    public abstract boolean isSub();

    public abstract void setReturn(SplElement typeValue);

    public abstract boolean interrupted();

    public abstract void breakLoop();

    public abstract void resumeLoop();

    public abstract void pauseLoop();

    public abstract void invalidate();

    public abstract void fallthrough();

    public abstract boolean isFallingThrough();

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
        VarEntry entry = innerGet(name, true, lineFile);
        if (entry == null)
            throw new EnvironmentError("Variable '" + name + "' is not defined in this scope. ", lineFile);

        if (entry.constant && entry.getValue() != Undefined.UNDEFINED) {
            throw new EnvironmentError("Constant '" + name + "' is not assignable. ", lineFile);
        }

        entry.setValue(value);
    }

    public SplElement get(String name, LineFile lineFile) {
        VarEntry se = innerGet(name, true, lineFile);
        if (se == null) {
            throw new EnvironmentError("Name '" + name + "' not found. ", lineFile);
        }

        return se.getValue();
    }

    public boolean hasName(String name, LineFile lineFile) {
        return innerGet(name, true, lineFile) != null;
    }

    /**
     * Get a value stored by name.
     * <p>
     * Note that this method is overridden in {@code InstanceEnvironment}
     *
     * @param name         the name
     * @param isFirst      whether this is called by another function. {@code false} if this call is self recursion
     * @param lineFile     line and file for error information
     * @return the value
     */
    protected VarEntry innerGet(String name, boolean isFirst, LineFile lineFile) {
        VarEntry tv = variables.get(name);

        if (tv == null) {
            if (outer != null) {
                tv = outer.innerGet(name, false, lineFile);
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
