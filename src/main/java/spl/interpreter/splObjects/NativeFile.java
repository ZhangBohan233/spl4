package spl.interpreter.splObjects;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NativeFile extends SplObject {

    private final RandomAccessFile raf;
    private final String mode;

    public NativeFile(String fileName, long mode) throws IOException {
        this.mode = getMode(mode);
        raf = new RandomAccessFile(fileName, this.mode);
    }

    public void read() {

    }

    public void close() throws IOException {
        raf.close();
    }

    private static String getMode(long mode) {
        return switch ((int) mode) {
            case 2 -> "w";
            case 3 -> "rb";
            case 4 -> "wb";
            default -> "r";
        };
    }
}
