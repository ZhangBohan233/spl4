package spl.interpreter.invokes;

import spl.ast.Arguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splObjects.NativeObject;
import spl.interpreter.splObjects.SplArray;
import spl.util.LineFilePos;

import java.io.FileInputStream;
import java.io.IOException;

public class NativeInFile extends NativeObject {

    private final FileInputStream stream;

    private NativeInFile(String fileName) throws IOException {
        this.stream = new FileInputStream(fileName);
    }

    public static NativeInFile create(String fileName) {
        try {
            return new NativeInFile(fileName);
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] read(int length) throws IOException {
        byte[] res = new byte[length];
        int read = stream.read(res);
        if (read == length) return res;
        else if (read <= 0) return new byte[0];
        else {
            byte[] trueRes = new byte[read];
            System.arraycopy(res, 0, trueRes, 0, read);
            return trueRes;
        }
    }

    /* Methods can be directly called by spl */

    @SuppressWarnings("unused")
    public Bool close(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "NativeInFile.close()", env, lineFilePos);
        try {
            stream.close();
            return Bool.TRUE;
        } catch (IOException e) {
            //
        }
        return Bool.FALSE;
    }

    @SuppressWarnings("unused")
    public Reference read(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 1, "NativeInFile.read()", env, lineFilePos);

        int readLen = (int) (args.getLine().get(0).evaluate(env)).intValue();
        try {
            return SplArray.fromJavaArray(read(readLen), env, lineFilePos);
        } catch (IOException e) {
            return Reference.NULL;
        }
    }
}
