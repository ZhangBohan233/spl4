package spl.interpreter.invokes;

import spl.ast.Arguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.splObjects.NativeObject;
import spl.interpreter.splObjects.SplArray;
import spl.util.LineFilePos;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;

public class NativeOutFile extends NativeObject {

    private final FileOutputStream fos;

    private NativeOutFile(String fileName) throws IOException {
        this.fos = new FileOutputStream(fileName);
    }

    public static NativeOutFile create(String fileName) {
        try {
            return new NativeOutFile(fileName);
        } catch (IOException e) {
            return null;
        }
    }

    private void write(byte[] data) throws IOException {
        fos.write(data);
    }

    /* Methods can be directly called by spl */

    @SuppressWarnings("unused")
    public Bool close(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "NativeOutFile.close()", env, lineFilePos);
        try {
            fos.close();
            return Bool.TRUE;
        } catch (IOException e) {
            //
        }
        return Bool.FALSE;
    }

    @SuppressWarnings("unused")
    public Bool flush(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 0, "NativeOutFile.flush()", env, lineFilePos);
        try {
            fos.flush();
            return Bool.TRUE;
        } catch (IOException e) {
            //
        }
        return Bool.FALSE;
    }

    @SuppressWarnings("unused")
    public Bool write(Arguments args, Environment env, LineFilePos lineFilePos) {
        checkArgCount(args, 1, "NativeOutFile.write()", env, lineFilePos);
        Reference ref = (Reference) args.getLine().get(0).evaluate(env);
        byte[] arr = SplArray.toJavaByteArray(ref, env.getMemory());
        try {
            write(arr);
            return Bool.TRUE;
        } catch (IOException e) {
            return Bool.FALSE;
        }
    }
}
