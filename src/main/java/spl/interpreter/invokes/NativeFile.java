package spl.interpreter.invokes;

import spl.ast.Arguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.splObjects.NativeObject;
import spl.util.LineFilePos;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NativeFile extends NativeObject {

    private final RandomAccessFile raf;
    private final String mode;

    public static NativeFile create(String fileName, long mode) {
        try {
            return new NativeFile(fileName, mode);
        } catch (IOException e) {
            return null;
        }
    }

    private NativeFile(String fileName, long mode) throws IOException {
        this.mode = getMode(mode);
        raf = new RandomAccessFile(fileName, this.mode);
    }

    public void read() {

    }

    public Bool close(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0,  "NativeFile.close()", env, lineFilePos);
        try {
            raf.close();
            return Bool.TRUE;
        } catch (IOException e) {
            //
        }
        return Bool.FALSE;
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
