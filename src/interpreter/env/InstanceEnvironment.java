package interpreter.env;

import interpreter.Memory;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.Instance;
import util.LineFile;

public class InstanceEnvironment extends MainAbstractEnvironment {

    /**
     * Store the reference to the environment where this instance is created. Used only for gc.
     */
    public final Environment creationEnvironment;
    private final String className;

    public InstanceEnvironment(String className, Environment definitionEnv, Environment creationEnvironment) {
        super(definitionEnv.getMemory(), definitionEnv);

        this.className = className;
        this.creationEnvironment = creationEnvironment;
    }

    public void directDefineConstAndSet(String name, SplElement value) {
        variables.put(name, VarEntry.constEntry(value));
    }

    @Override
    public boolean interrupted() {
        return false;
    }

    @Override
    protected VarEntry innerGet(String name, boolean isFirst) {
        VarEntry tv = searchSuper(name);
        if (tv == null)
            return super.innerGet(name, isFirst);
        else return tv;
    }

    private VarEntry searchSuper(String name) {
        VarEntry tv = variables.get(name);

        if (tv == null) {
            VarEntry superTv = variables.get("super");
            if (superTv == null) return null;
            else {
                Instance instance = (Instance) getMemory().get((Pointer) superTv.getValue());
                return instance.getEnv().searchSuper(name);
            }
        } else return tv;
    }

    public boolean selfContains(String name) {
        return variables.containsKey(name);
    }

    @Override
    public String toString() {
        return "InstanceEnv of '" + className + "'";
    }
}
