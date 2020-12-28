package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplObject;
import spl.util.Constants;
import spl.util.LineFile;

import java.util.List;

public class IndexingNode extends Expression {

    private final Node callObj;
    private final Line args;

    public IndexingNode(Node callObj, Line args, LineFile lineFile) {
        super(lineFile);

        this.callObj = callObj;
        this.args = args;
    }

    public static int getIndex(List<Node> arguments, Environment env) {
        if (arguments.size() != 1) {
            return -2;
        }
        SplElement index = arguments.get(0).evaluate(env);
        if (!index.isIntLike()) {
            return -1;
        }
        return (int) index.intValue();
    }

    public Line getArgs() {
        return args;
    }

    public Node getCallObj() {
        return callObj;
    }

    @Override
    protected SplElement internalEval(Environment env) {

        return crossEnvEval(env, env);
    }

    public SplElement crossEnvEval(Environment definitionEnv, Environment callEnv) {
        SplElement callRes = getCallObj().evaluate(definitionEnv);
        List<Node> arguments = getArgs().getChildren();
        int index = getIndex(arguments, callEnv);
        if (index < 0) {
            if (index == -2)
                return SplInvokes.throwExceptionWithError(
                        callEnv,
                        Constants.TYPE_ERROR,
                        "Index can only have one part.",
                        lineFile);
            else
                return SplInvokes.throwExceptionWithError(
                        callEnv,
                        Constants.TYPE_ERROR,
                        "Index must be int.",
                        lineFile);
        }

        Pointer objPtr = (Pointer) callRes;

        SplObject obj = callEnv.getMemory().get(objPtr);

        if (obj instanceof SplArray) {
            return SplArray.getItemAtIndex(objPtr, index, callEnv, lineFile);
        } else if (obj instanceof Instance) {
            Instance ins = (Instance) obj;
            SplMethod getItemFn = (SplMethod)
                    callEnv.getMemory().get((Pointer) ins.getEnv().get(Constants.GET_ITEM_FN, lineFile));
            return getItemFn.call(EvaluatedArguments.of(objPtr, new Int(index)), callEnv, lineFile);
        } else {
            return SplInvokes.throwExceptionWithError(
                    callEnv,
                    Constants.TYPE_ERROR,
                    "Right side of indexing must be array or instance.",
                    lineFile);
        }
    }

    @Override
    public String toString() {
        return callObj + " " + args;
    }
}
