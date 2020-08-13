package spl.interpreter.env;

import spl.interpreter.Memory;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

import java.util.HashSet;
import java.util.Set;

public abstract class MainAbstractEnvironment extends Environment {

    protected Set<ModuleEnvironment> namespaces = new HashSet<>();

    public MainAbstractEnvironment(Memory memory, Environment outer) {
        super(memory, outer);
    }

    @Override
    public boolean isSub() {
        return false;
    }

    @Override
    public void setReturn(SplElement element) {
        throw new EnvironmentError("Return outside function. ");
    }

    @Override
    public void addNamespace(ModuleEnvironment moduleEnvironment) {
        namespaces.add(moduleEnvironment);
    }

    @Override
    protected VarEntry searchInNamespaces(String name) {
        for (ModuleEnvironment me : namespaces) {
            VarEntry tv = me.variables.get(name);
            if (tv != null) return tv;
        }
        if (outer == null) return null;
        else return outer.searchInNamespaces(name);
    }

    @Override
    protected void setInNamespaces(String name, SplElement typeValue) {
//        System.out.println(12312313);
    }

    public void breakLoop() {
        throw new EnvironmentError("Break outside loop");
    }

    public void resumeLoop() {
        throw new EnvironmentError("Outside function");
    }

    public void pauseLoop() {
        throw new EnvironmentError("Continue outside function");
    }

    public void invalidate() {
        throw new EnvironmentError();
    }

    @Override
    public void fallthrough(LineFile lineFile) {
        throw new EnvironmentError("'fallthrough' outside case statements. ", lineFile);
    }

    @Override
    public boolean isFallingThrough() {
        throw new EnvironmentError("'fallthrough' outside case statements. ");
    }

    @Override
    public void yield(SplElement value, LineFile lineFile) {
        throw new EnvironmentError("'yield' outside cond/switch-case expressions. ", lineFile);
    }

    @Override
    public SplElement yieldResult() {
        throw new EnvironmentError("'yield' outside cond/switch-case expressions. ");
    }

}
