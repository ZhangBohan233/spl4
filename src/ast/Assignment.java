package ast;

import interpreter.splErrors.NativeError;
import interpreter.env.Environment;
import interpreter.primitives.Int;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splErrors.TypeError;
import interpreter.splObjects.*;
import util.LineFile;

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

                // todo: custom class set-item
                if (obj instanceof SplArray) {
                    SplArray.setItemAtIndex(arrPtr, (int) index.value, value, env, lineFile);
                } else {
                    throw new NativeError("Object '" + obj + "' does not support set-item. ", lineFile);
                }
            } else {
                throw new NativeError("Array creation must take exactly one int as argument. ", lineFile);
            }
//            TypeValue leftCallRes = ((IndexingNode) key).getCallObj().evaluate(env);
//            List<Node> arguments = ((IndexingNode) key).getArgs().getChildren();
//            int index = IndexingNode.getIndex(leftCallRes, arguments, env, lineFile);
////            System.out.println(key + " " + key.getLineFile().toStringFileLine());
////            System.out.println("Key is " + leftCallRes + ", Obj is " +
////                    env.getMemory().get((Pointer) leftCallRes.getValue()));
//            SplArray.setItemAtIndex((Pointer) leftCallRes.getValue(),
//                    index,
//                    (ArrayType) leftCallRes.getType(),
//                    value,
//                    env,
//                    lineFile);
        } else {
            throw new NativeError();
        }
    }
}
