package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.*;
import spl.util.BytesIn;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

public class Assignment extends BinaryExpr {
    public Assignment(LineFilePos lineFile) {
        super("=", false, lineFile);
    }

    public static void assignment(Node key, SplElement value, Environment env, LineFilePos lineFile) {
        if (key instanceof NameNode) {
            env.setVar(((NameNode) key).getName(), value, lineFile);
        } else if (key instanceof Declaration) {
            key.evaluate(env);
            env.setVar(((Declaration) key).declaredName, value, lineFile);
        } else if (key instanceof Dot) {
            SplElement dotLeft = ((Dot) key).left.evaluate(env);
            if (!(dotLeft instanceof Reference)) {
                SplInvokes.throwException(
                        env,
                        Constants.TYPE_ERROR,
                        "Left side of dot must be instance or module",
                        lineFile);
                return;
            }
            SplObject dotLeftObj = env.getMemory().get((Reference) dotLeft);
            Environment objEnv;
            if (dotLeftObj instanceof SplModule) {
                objEnv = ((SplModule) dotLeftObj).getEnv();
            } else if (dotLeftObj instanceof Instance) {
                objEnv = ((Instance) dotLeftObj).getEnv();
            } else {
                SplInvokes.throwException(
                        env,
                        Constants.TYPE_ERROR,
                        "Left side of dot must be instance or module",
                        lineFile);
                return;
            }
            objEnv.setVar(((NameNode) ((Dot) key).right).getName(), value, lineFile);
        } else if (key instanceof IndexingNode) {
            IndexingNode indexingNode = (IndexingNode) key;
            SplElement arrPtrRaw = indexingNode.getCallObj().evaluate(env);
            if (arrPtrRaw == Undefined.ERROR) return;
            Reference arrPtr = (Reference) arrPtrRaw;
            if (indexingNode.getArgs().getChildren().size() == 1) {
                SplElement index = indexingNode.getArgs().getChildren().get(0).evaluate(env);
                SplObject obj = env.getMemory().get(arrPtr);

                if (obj instanceof SplArray) {
                    if (!(index instanceof Int)) {
                        SplInvokes.throwException(
                                env,
                                Constants.TYPE_ERROR,
                                "Array index must be int.",
                                lineFile
                        );
                        return;
                    }
                    SplArray.setItemAtIndex(arrPtr, (int) ((Int) index).value, value, env, lineFile);
                } else if (obj instanceof Instance) {
                    Instance ins = (Instance) obj;
                    SplElement setItem = ins.getEnv().get(Constants.SET_ITEM_FN, lineFile);
                    if (setItem == Undefined.ERROR) return;

                    SplMethod setItemFn = env.getMemory().get((Reference) setItem);
                    setItemFn.call(EvaluatedArguments.of(arrPtr, index, value), env, lineFile);
                } else {
                    SplInvokes.throwException(
                            env,
                            Constants.TYPE_ERROR,
                            "Object '" + obj + "' does not support set-item.",
                            lineFile);
                }
            } else {
                SplInvokes.throwException(
                        env,
                        Constants.TYPE_ERROR,
                        "Array creation must take exactly one int as argument.",
                        lineFile);
            }
        } else {
            SplInvokes.throwException(
                    env,
                    Constants.TYPE_ERROR,
                    "",
                    lineFile);
        }
    }

    public static Assignment reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        is.readString();
        Expression left = Reconstructor.reconstruct(is);
        Expression right = Reconstructor.reconstruct(is);

        Assignment ass = new Assignment(lineFilePos);
        ass.setLeft(left);
        ass.setRight(right);
        return ass;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        SplElement rightRes = right.evaluate(env);

        assignment(left, rightRes, env, getLineFile());
        env.getMemory().clearNewPointers(env.getThreadId());
        return rightRes;
    }
}
