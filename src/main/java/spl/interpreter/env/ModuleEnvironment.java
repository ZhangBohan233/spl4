package spl.interpreter.env;

public class ModuleEnvironment extends MainAbstractEnvironment {

    private final String modulePath;

    public ModuleEnvironment(String modulePath, Environment outer) {
        super(outer.memory, outer);

        this.modulePath = modulePath;
    }

    @Override
    public boolean interrupted() {
        return false;
    }

    public String getModulePath() {
        return modulePath;
    }

    @Override
    public String toString() {
        return "ModuleEnvironment@" + modulePath;
    }
}
