package spl.interpreter.env;

import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public abstract class SubAbstractEnvironment extends Environment {

    public SubAbstractEnvironment(Environment outer) {
        super(outer.memory, outer);
    }

    @Override
    public boolean isSub() {
        return true;
    }

    @Override
    public void setReturn(SplElement typeValue, LineFilePos lineFile) {
        outer.setReturn(typeValue, lineFile);
    }

    @Override
    public void addNamespace(ModuleEnvironment moduleEnvironment) {
        throw new EnvironmentError();
    }

    @Override
    protected VarEntry searchInNamespaces(String name) {
        return outer.searchInNamespaces(name);
    }

    @Override
    protected void setInNamespaces(String name, SplElement typeValue) {

    }

    @Override
    public void fallthrough(LineFilePos lineFile) {
        outer.fallthrough(lineFile);
    }

    @Override
    public boolean isFallingThrough() {
        return outer.isFallingThrough();
    }

    @Override
    public void yield(SplElement value, LineFilePos lineFile) {
        outer.yield(value, lineFile);
    }

    @Override
    public SplElement yieldResult() {
        return outer.yieldResult();
    }
}
