package spl.interpreter.env;

public class ModuleEnvironment extends MainAbstractEnvironment {

    private final String moduleName;

    public ModuleEnvironment(String moduleName, Environment outer) {
        super(outer.memory, outer);

        this.moduleName = moduleName;
    }

    @Override
    public boolean interrupted() {
        return false;
    }

    public String getModuleName() {
        return moduleName;
    }
}
