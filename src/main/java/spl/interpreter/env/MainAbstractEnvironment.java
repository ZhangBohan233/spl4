package spl.interpreter.env;

import spl.interpreter.Memory;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.SplElement;
import spl.util.Constants;
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
    public void setReturn(SplElement element, LineFile lineFile) {
//        throw new EnvironmentError("Return outside function. ");
        SplInvokes.throwException(this, Constants.NAME_ERROR, "Return outside function.",
                lineFile);
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

    @Override
    public void breakLoop(LineFile lineFile) {
//        throw new EnvironmentError("Break outside loop");
        SplInvokes.throwException(this, Constants.NAME_ERROR, "Break outside loop",
                lineFile);
    }

    @Override
    public void resumeLoop() {
        throw new EnvironmentError("Outside function");  // do not change this
    }

    @Override
    public void pauseLoop(LineFile lineFile) {
//        throw new EnvironmentError("Continue outside function");
        SplInvokes.throwException(this, Constants.NAME_ERROR, "Continue outside function",
                lineFile);
    }

    @Override
    public void invalidate() {
        throw new EnvironmentError();
    }

    @Override
    public void fallthrough(LineFile lineFile) {
//        throw new EnvironmentError("'fallthrough' outside case statements. ", lineFile);
        SplInvokes.throwException(this, Constants.NAME_ERROR, "'fallthrough' outside case statements.",
                lineFile);
    }

    @Override
    public boolean isFallingThrough() {
        throw new EnvironmentError("'fallthrough' outside case statements. ");  // do not change this
    }

    @Override
    public void yield(SplElement value, LineFile lineFile) {
//        throw new EnvironmentError("'yield' outside cond/switch-case expressions. ", lineFile);
        SplInvokes.throwException(this, Constants.NAME_ERROR, "'yield' outside cond/switch-case expressions.",
                lineFile);
    }

    @Override
    public SplElement yieldResult() {
        throw new EnvironmentError("'yield' outside cond/switch-case expressions. ");  // do not change this
    }

}
