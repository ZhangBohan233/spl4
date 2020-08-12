package interpreter.env;

import interpreter.Memory;
import interpreter.primitives.Pointer;
import interpreter.splObjects.SplModule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GlobalEnvironment extends MainAbstractEnvironment {

    /**
     * A map to avoided duplicate module in memory.
     */
    private final Map<String, Pointer> importedModules = new HashMap<>();

    protected Pointer exceptionPtr;

    public GlobalEnvironment(Memory memory) {
        super(memory, null);
    }

    public void throwException(Pointer exceptionPtr) {
        this.exceptionPtr = exceptionPtr;
    }

    public boolean hasException() {
        return exceptionPtr != null;
    }

    @Override
    public boolean interrupted() {
        return false;
    }

    public Pointer getExceptionPtr() {
        return exceptionPtr;
    }

    public void removeException() {
        exceptionPtr = null;
    }

    @Override
    public Pointer getImportedModulePtr(String modulePath) {
        return importedModules.get(modulePath);
    }

    @Override
    public void addImportedModulePtr(String modulePath, Pointer modulePtr) {
        importedModules.put(modulePath, modulePtr);
    }
}
