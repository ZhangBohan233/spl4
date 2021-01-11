package spl.interpreter.invokes;

import spl.ast.Arguments;
import spl.ast.StringLiteral;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splObjects.NativeObject;
import spl.interpreter.splObjects.SplArray;
import spl.util.LineFilePos;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NativeFile extends NativeObject {

    private final RandomAccessFile raf;
    private final String mode;
    private final long length;

    public static NativeFile create(String fileName, long mode) {
        try {
            return new NativeFile(fileName, mode);
        } catch (IOException e) {
            return null;
        }
    }

    private NativeFile(String fileName, long mode) throws IOException {
        this.mode = getMode(mode);
        raf = new RandomAccessFile(fileName, "rw");
        this.length = raf.length();
    }

    private byte[] read(int length) {
        try {
            byte[] res = new byte[length];
            int read = raf.read(res);
            if (read == length) return res;
            else {
                byte[] trueRes = new byte[read];
                System.arraycopy(res, 0, trueRes, 0, read);
                return trueRes;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public boolean write(byte[] bytes) {
        try {
            raf.write(bytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /* Methods can be directly called by spl */

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

    public Int length(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "NativeFile.length()", env, lineFilePos);
        return new Int(length);
    }

    public Int position(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "NativeFile.length()", env, lineFilePos);
        try {
            return new Int(raf.getFilePointer());
        } catch (IOException e) {
            return Int.NEG_ONE;
        }
    }

    public Reference readText(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 1, "NativeFile.readText()", env, lineFilePos);

        int readLen = (int) (args.getLine().get(0).evaluate(env)).intValue();
        byte[] read = read(readLen);
        if (read == null) return Reference.NULL;
        String s = new String(read);
        return StringLiteral.createString(s.toCharArray(), env, lineFilePos);
    }

    public Reference readBytes(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 1, "NativeFile.readText()", env, lineFilePos);

        int readLen = (int) (args.getLine().get(0).evaluate(env)).intValue();
        byte[] read = read(readLen);
        if (read == null) return Reference.NULL;
        return SplArray.fromJavaArray(read, env, lineFilePos);
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
