package spl.tools.codeArea;

import spl.util.Utilities;

import java.io.File;
import java.io.IOException;

public class CodeFile {

    private File file;

    public CodeFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void save(String text) throws IOException {
        Utilities.writeFile(file, text);
    }
}
