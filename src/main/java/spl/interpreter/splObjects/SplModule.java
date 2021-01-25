package spl.interpreter.splObjects;

import spl.interpreter.env.ModuleEnvironment;

public class SplModule extends NativeObject {

    private final String importedPath;
    private final ModuleEnvironment env;

    public SplModule(String importedPath, ModuleEnvironment env) {
        this.importedPath = importedPath;
        this.env = env;
    }

    public ModuleEnvironment getEnv() {
        return env;
    }

    @Override
    public String toString() {
        return "Module " + importedPath;
    }
}
