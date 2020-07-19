package interpreter.env;

import interpreter.Memory;
import interpreter.primitives.Pointer;
import interpreter.splObjects.SplModule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GlobalEnvironment extends MainAbstractEnvironment {

    private final Map<File, Pointer> importedModules = new HashMap<>();

    public GlobalEnvironment(Memory memory) {
        super(memory, null);
    }

    @Override
    public boolean interrupted() {
        return false;
    }

    @Override
    public Pointer getImportedModulePtr(File moduleFile) {
        return importedModules.get(moduleFile);
    }

    @Override
    public void addImportedModulePtr(File moduleFile, Pointer modulePtr) {
        importedModules.put(moduleFile, modulePtr);
    }
}
