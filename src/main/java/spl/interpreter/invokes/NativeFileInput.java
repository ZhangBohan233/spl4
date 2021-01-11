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
import java.io.InputStream;

public class NativeFileInput extends NativeObject {

    private final InputStream stream;

    private NativeFileInput(String fileName) throws IOException {
        this.stream = new FileInputStream(fileName);
    }

    public static NativeFileInput create(String fileName) {
        try {
            return new NativeFileInput(fileName);
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

    public Bool close(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "NativeFileInput.close()", env, lineFilePos);
        try {
            stream.close();
            return Bool.TRUE;
        } catch (IOException e) {
            //
        }
        return Bool.FALSE;
    }

    public Reference read(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 1, "NativeFileInput.read()", env, lineFilePos);

        int readLen = (int) (args.getLine().get(0).evaluate(env)).intValue();
        try {
            return SplArray.fromJavaArray(read(readLen), env, lineFilePos);
        } catch (IOException e) {
            return Reference.NULL;
        }
    }
}
