package spl.interpreter.env;

import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public abstract class SubAbstractEnvironment extends Environment {

    public SubAbstractEnvironment(Environment outer) {
        super(outer.memory, outer);
    }

//    @Override
//    public void defineFunction(String name, TypeValue funcTv, LineFile lineFile) {
//        throw new EnvironmentError();
//    }

    @Override
    public boolean isSub() {
        return true;
    }

    @Override
    public void setReturn(SplElement typeValue) {
        outer.setReturn(typeValue);
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
    public void fallthrough(LineFile lineFile) {
        outer.fallthrough(lineFile);
    }

    @Override
    public boolean isFallingThrough() {
        return outer.isFallingThrough();
    }

    @Override
    public void yield(SplElement value, LineFile lineFile) {
        outer.yield(value, lineFile);
    }

    @Override
    public SplElement yieldResult() {
        return outer.yieldResult();
    }
}
