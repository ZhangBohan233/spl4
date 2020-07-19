package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.*;
import interpreter.types.*;
import util.LineFile;

import java.util.List;

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
//            SplElement leftLeft = ((Dot) key).left.evaluate(env);
//            PointerType leftLeftType = (PointerType) leftLeft.getType();
//            Environment leftEnv;
//            if (leftLeftType.getPointerType() == PointerType.MODULE_TYPE) {
//                SplModule leftModule = (SplModule) env.getMemory().get((Pointer) leftLeft.getValue());
//                leftEnv = leftModule.getEnv();
//            } else if (leftLeftType.getPointerType() == PointerType.CLASS_TYPE) {
//                Instance leftModule = (Instance) env.getMemory().get((Pointer) leftLeft.getValue());
//                leftEnv = leftModule.getEnv();
//            } else {
//                throw new TypeError();
//            }
//            leftEnv.setVar(((NameNode) ((Dot) key).right).getName(), value, lineFile);
        } else if (key instanceof IndexingNode) {
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
            throw new SplException();
        }
    }
}
