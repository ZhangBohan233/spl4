package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Bool;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.util.*;

import java.io.IOException;
import java.io.PrintStream;

public class AssertStmt extends UnaryStmt {

    public AssertStmt(LineFilePos lineFile) {
        super("assert", true, lineFile);
    }

    public static AssertStmt reconstruct(BytesIn bytesIn, LineFilePos lineFilePos) throws Exception {
        Expression value = Reconstructor.reconstruct(bytesIn);
        AssertStmt as = new AssertStmt(lineFilePos);
        as.setValue(value);
        return as;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        value.save(out);
    }

    @Override
    protected void internalProcess(Environment env) {
        if (!env.getMemory().isCheckAssert()) return;
        SplElement val = value.evaluate(env);
        if (val instanceof Bool) {
            if (((Bool) val).value) return;
            else {
                SplInvokes.throwException(
                        env,
                        Constants.ASSERTION_ERROR,
                        "Assertion failed.",
                        lineFile
                );
            }
            return;
        } else if (Utilities.isInstancePtr(val, "Boolean", env, lineFile)) {
            Instance ins = env.getMemory().get((Reference) val);
            Bool value = (Bool) ins.getEnv().get(Constants.WRAPPER_ATTR, lineFile);
            if (value.value) return;
            else SplInvokes.throwException(
                    env,
                    Constants.ASSERTION_ERROR,
                    "Assertion failed.",
                    lineFile
            );
            return;
        }
        SplInvokes.throwException(
                env,
                Constants.TYPE_ERROR,
                "Assert statement checks boolean.",
                lineFile
        );
    }
}
