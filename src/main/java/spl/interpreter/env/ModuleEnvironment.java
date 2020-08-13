package spl.interpreter.env;

public class ModuleEnvironment extends MainAbstractEnvironment {

    public ModuleEnvironment(Environment outer) {
        super(outer.memory, outer);
    }

    @Override
    public boolean interrupted() {
        return false;
    }
}
