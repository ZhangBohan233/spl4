package interpreter.env;

import interpreter.Memory;
import interpreter.primitives.SplElement;
import util.LineFile;
import util.Utilities;

import java.util.Map;

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
    public void fallthrough() {
        outer.fallthrough();
    }

    @Override
    public boolean isFallingThrough() {
        return outer.isFallingThrough();
    }
}
