package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splErrors.NativeError;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;

import java.io.IOException;

public class ImportStmt extends Statement {

    private final String importName;
    private final String path;

    public ImportStmt(String path, String importName, LineFilePos lineFile) {
        super(lineFile);

        this.path = path;
        this.importName = importName;
    }

    public static ImportStmt reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        String path = in.readString();
        String name = in.readString();
        return new ImportStmt(path, name, lineFilePos);
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(path);
        out.writeString(importName);
    }

    @Override
    protected void internalProcess(Environment env) {
        // This step is to avoid duplicate module creation.
        // Any import from a same file should point to a same module
        Reference ptr = env.getImportedModulePtr(path);
        if (ptr == null) {
            throw new NativeError("Unexpected error when importing. ", lineFile);
        }

        env.defineVarAndSet(importName, ptr, getLineFile());
    }

    @Override
    public String toString() {
        return "Import '" + path + "' as '" + importName + "'";
    }

    public String getPath() {
        return path;
    }

    public String getImportName() {
        return importName;
    }
}
