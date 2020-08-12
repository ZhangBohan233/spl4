package ast;

import interpreter.env.Environment;
import interpreter.env.ModuleEnvironment;
import interpreter.primitives.SplElement;
import interpreter.primitives.Pointer;
import interpreter.splErrors.NativeError;
import interpreter.splObjects.SplModule;
import util.LineFile;

import java.io.File;

public class ImportStmt extends AbstractStatement {

    private final String importName;
    private final String path;

    public ImportStmt(String path, String importName, LineFile lineFile) {
        super(lineFile);

        this.path = path;
        this.importName = importName;
    }

    @Override
    protected void internalProcess(Environment env) {

        // This step is to avoid duplicate module creation.
        // Any import from a same file should point to a same module
        Pointer ptr = env.getImportedModulePtr(path);
        if (ptr == null) {
            throw new NativeError("Unexpected error when importing. ", lineFile);
        }

        env.defineVarAndSet(importName, ptr, getLineFile());
    }

    @Override
    public String toString() {
        return "Import '" + path + "' as '" + importName + "'";
    }
}
