package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.splErrors.NativeError;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splErrors.TypeError;
import spl.interpreter.splObjects.*;
import spl.util.Constants;
import spl.util.LineFile;

public class Assignment extends BinaryExpr {
    public Assignment(LineFile lineFile) {
        super("=", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement rightRes = right.evaluate(env);

        assignment(left, rightRes, env, getLineFile());
        return rightRes;
    }

    public static void assignment(Node key, SplElement value, Environment env, LineFile lineFile) {
        if (key instanceof NameNode) {
            env.setVar(((NameNode) key).getName(), value, lineFile);
        } else if (key instanceof Declaration) {
            key.evaluate(env);

            env.setVar(((Declaration) key).declaredName, value, lineFile);
        } else if (key instanceof Dot) {
            SplElement dotLeft = ((Dot) key).left.evaluate(env);
            if (!(dotLeft instanceof Pointer)) {
                throw new TypeError("Left side of dot must be instance or module", lineFile);
            }
            SplObject dotLeftObj = env.getMemory().get((Pointer) dotLeft);
            Environment objEnv;
            if (dotLeftObj instanceof SplModule) {
                objEnv = ((SplModule) dotLeftObj).getEnv();
            } else if (dotLeftObj instanceof Instance) {
                objEnv = ((Instance) dotLeftObj).getEnv();
            } else {
                throw new TypeError("Left side of dot must be instance or module", lineFile);
            }
            objEnv.setVar(((NameNode) ((Dot) key).right).getName(), value, lineFile);
        } else if (key instanceof IndexingNode) {
            IndexingNode indexingNode = (IndexingNode) key;
            Pointer arrPtr = (Pointer) indexingNode.getCallObj().evaluate(env);
            if (indexingNode.getArgs().getChildren().size() == 1) {
                Int index = (Int) indexingNode.getArgs().getChildren().get(0).evaluate(env);
                SplObject obj = env.getMemory().get(arrPtr);

                if (obj instanceof SplArray) {
                    SplArray.setItemAtIndex(arrPtr, (int) index.value, value, env, lineFile);
                } else if (obj instanceof Instance) {
                    Instance ins = (Instance) obj;
                    SplMethod setItemFn = (SplMethod)
                            env.getMemory().get((Pointer) ins.getEnv().get(Constants.SET_ITEM_FN, lineFile));
                    setItemFn.call(EvaluatedArguments.of(arrPtr, index, value), env, lineFile);
                } else {
                    throw new NativeError("Object '" + obj + "' does not support set-item. ", lineFile);
                }
            } else {
                throw new NativeError("Array creation must take exactly one int as argument. ", lineFile);
            }
        } else {
            throw new NativeError();
        }
    }
}
