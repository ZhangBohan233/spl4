package spl.interpreter.env;

import spl.interpreter.Memory;
import spl.interpreter.primitives.Reference;

import java.util.HashMap;
import java.util.Map;

public class GlobalEnvironment extends MainAbstractEnvironment {

    /**
     * A map to avoided duplicate module in memory.
     */
    private final Map<String, Reference> importedModules = new HashMap<>();

    protected Reference exceptionInsPtr;

    public GlobalEnvironment(Memory memory) {
        super(memory, null);
    }

    public void throwException(Reference exceptionInsPtr) {
        this.exceptionInsPtr = exceptionInsPtr;
    }

    public boolean hasException() {
        return exceptionInsPtr != null;
    }

    @Override
    public boolean interrupted() {
        return false;
    }

    public Reference getExceptionInsPtr() {
        return exceptionInsPtr;
    }

    /**
     * Removes exception, to make all sub-environments work normally
     */
    public void removeException() {
        exceptionInsPtr = null;
    }

    @Override
    public Reference getImportedModulePtr(String modulePath) {
        return importedModules.get(modulePath);
    }

    @Override
    public void addImportedModulePtr(String modulePath, Reference modulePtr) {
        importedModules.put(modulePath, modulePtr);
    }
}
