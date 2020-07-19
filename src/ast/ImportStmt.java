package ast;

import interpreter.env.Environment;
import interpreter.env.ModuleEnvironment;
import interpreter.primitives.SplElement;
import interpreter.primitives.Pointer;
import interpreter.splObjects.SplModule;
import util.LineFile;

import java.io.File;

public class ImportStmt extends Node {

    private final String importName;
    private final File file;
    private final BlockStmt content;

    public ImportStmt(String importName, File file, BlockStmt imported, LineFile lineFile) {
        super(lineFile);

        this.importName = importName;
        this.file = file;
        this.content = imported;
    }

    @Override
    protected SplElement internalEval(Environment env) {

        // This step is to avoid duplicate module creation.
        // Any import from a same file should point to a same module
        Pointer ptr = env.getImportedModulePtr(file);
        if (ptr == null) {
            ModuleEnvironment moduleScope = new ModuleEnvironment(env);
            content.evaluate(moduleScope);
            SplModule module = new SplModule(importName, moduleScope);

            ptr = env.getMemory().allocate(1, moduleScope);
            env.getMemory().set(ptr, module);

            env.addImportedModulePtr(file, ptr);
        }

        env.defineVarAndSet(importName, ptr, getLineFile());

        return null;
    }

    @Override
    public String toString() {
        return "Import as '" + importName + "'";
    }
}
