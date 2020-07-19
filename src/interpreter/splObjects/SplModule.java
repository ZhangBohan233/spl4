package interpreter.splObjects;

import interpreter.env.ModuleEnvironment;

public class SplModule extends SplObject {

    private String importName;
    private ModuleEnvironment env;

    public SplModule(String importName, ModuleEnvironment env) {
        this.importName = importName;
        this.env = env;
    }

    public ModuleEnvironment getEnv() {
        return env;
    }

    @Override
    public String toString() {
        return "Module " + importName;
    }
}
