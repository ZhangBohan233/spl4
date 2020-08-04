package ast;

import interpreter.EvaluatedArguments;
import interpreter.env.Environment;
import interpreter.primitives.Int;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splErrors.TypeError;
import interpreter.splObjects.Function;
import interpreter.splObjects.Instance;
import interpreter.splObjects.SplArray;
import interpreter.splObjects.SplObject;
import util.Constants;
import util.LineFile;

import java.util.List;

public class IndexingNode extends AbstractExpression  {

    private final Node callObj;
    private final Line args;

    public IndexingNode(Node callObj, Line args, LineFile lineFile) {
        super(lineFile);

        this.callObj = callObj;
        this.args = args;
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
        int index = getIndex(arguments, callEnv, getLineFile());

        Pointer objPtr = (Pointer) callRes;

        SplObject obj = callEnv.getMemory().get(objPtr);

        if (obj instanceof SplArray) {
            return SplArray.getItemAtIndex(objPtr, index, callEnv.getMemory(), lineFile);
        } else if (obj instanceof Instance) {
            Instance ins = (Instance) obj;
            Function getItemFn = (Function)
                    callEnv.getMemory().get((Pointer) ins.getEnv().get(Constants.GET_ITEM_FN, lineFile));
            return getItemFn.call(EvaluatedArguments.of(new Int(index)), callEnv, lineFile);
        } else {
            throw new TypeError(lineFile);
        }
    }

    @Override
    public String toString() {
        return callObj + " " + args;
    }

    public static int getIndex(List<Node> arguments, Environment env, LineFile lineFile) {
        if (arguments.size() != 1) {
            throw new TypeError("Indexing must have 1 index. ", lineFile);
        }
        SplElement index = arguments.get(0).evaluate(env);
        if (!index.isIntLike()) {
            throw new TypeError("Indexing must be int. ", lineFile);
        }
        return (int) index.intValue();
    }
}
