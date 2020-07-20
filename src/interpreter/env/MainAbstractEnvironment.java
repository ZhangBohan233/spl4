package interpreter.env;

import interpreter.Memory;
import interpreter.primitives.SplElement;
import interpreter.types.TypeError;
import util.LineFile;
import util.Utilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class MainAbstractEnvironment extends Environment {

//    public static final Set<String> NON_OVERRIDE_FUNCTIONS = Set.of(
//            "init"
//    );

    protected Set<ModuleEnvironment> namespaces = new HashSet<>();

    public MainAbstractEnvironment(Memory memory, Environment outer) {
        super(memory, outer);
    }

//    @Override
//    public void defineFunction(String name, TypeValue funcTv, LineFile lineFile) {
//        if (!NON_OVERRIDE_FUNCTIONS.contains(name) && localHasName(name, lineFile)) {
//            TypeValue superFn = get(name, lineFile);
//            // FIXME: this step might not be useful anymore
//            if (!superFn.getType().equals(funcTv.getType())) {
//                throw new TypeError("Function '" + name + "' does not overrides its super function, but has " +
//                        "identical names. ", lineFile);
//            }
//        }
//
//        variables.put(name, funcTv);
//    }

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
    public void fallthrough() {
        throw new EnvironmentError("'fallthrough' outside case statements. ");
    }

    @Override
    public boolean isFallingThrough() {
        throw new EnvironmentError("'fallthrough' outside case statements. ");
    }
}
