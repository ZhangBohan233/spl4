package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Int;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplArray;
import spl.interpreter.splObjects.SplMethod;
import spl.interpreter.splObjects.SplObject;
import spl.lexer.SyntaxError;
import spl.util.*;

import java.io.IOException;
import java.util.List;

public class IndexingNode extends Expression {

    private final Expression callObj;
    private final Line args;
    private DictSetLiteral initialValue;

    public IndexingNode(Expression callObj, Line args, LineFilePos lineFile) {
        super(lineFile);

        this.callObj = callObj;
        this.args = args;
    }

    public static IndexingNode reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        Expression callObj = Reconstructor.reconstruct(in);
        Line args = Reconstructor.reconstruct(in);
        var node = new IndexingNode(callObj, args, lineFilePos);
        node.setInitialValue(in.readOptional());
        return node;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        callObj.save(out);
        args.save(out);
        out.writeOptional(initialValue);
    }

    public void setInitialValue(DictSetLiteral initialValue) {
        if (initialValue.isDict())
            throw new SyntaxError("Direct array creation must not contain assignments. ",
                initialValue.getLineFile());
        this.initialValue = initialValue;
    }

    public DictSetLiteral getInitialValue() {
        return initialValue;
    }

    public Line getArgs() {
        return args;
    }

    public Expression getCallObj() {
        return callObj;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return crossEnvEval(env, env);
    }

    public SplElement crossEnvEval(Environment definitionEnv, Environment callEnv) {
        SplElement callRes = getCallObj().evaluate(definitionEnv);
        List<Node> arguments = getArgs().getChildren();

        if (arguments.size() != 1)
            return SplInvokes.throwExceptionWithError(
                    callEnv,
                    Constants.TYPE_ERROR,
                    "Index can only have one part.",
                    lineFile);

        SplElement indexEle = arguments.get(0).evaluate(callEnv);
        if (indexEle.isIntLike()) {
            int index = (int) indexEle.intValue();

            Reference objPtr = (Reference) callRes;
            SplObject obj = callEnv.getMemory().get(objPtr);

            if (obj instanceof SplArray) {
                return SplArray.getItemAtIndex(objPtr, index, callEnv, lineFile);
            } else if (obj instanceof Instance) {
                Instance ins = (Instance) obj;
                SplElement getItemFnEle = ins.getEnv().get(Constants.GET_ITEM_FN, lineFile);
                if (getItemFnEle == Undefined.ERROR) return getItemFnEle;
                SplMethod getItemFn =
                        callEnv.getMemory().get((Reference) getItemFnEle);
                return getItemFn.call(EvaluatedArguments.of(objPtr, new Int(index)), callEnv, lineFile);
            } else {
                return SplInvokes.throwExceptionWithError(
                        callEnv,
                        Constants.TYPE_ERROR,
                        "Right side of indexing must be array or instance.",
                        lineFile);
            }
        } else {
            return nonIntIndex(callRes, indexEle, callEnv);
        }
    }

    private SplElement nonIntIndex(SplElement callRes, SplElement indexEle, Environment callEnv) {
        Reference objPtr = (Reference) callRes;
        SplObject obj = callEnv.getMemory().get(objPtr);

        if (obj instanceof Instance) {
            Instance ins = (Instance) obj;
            SplElement getItemFnEle = ins.getEnv().get(Constants.GET_ITEM_FN, lineFile);
            if (getItemFnEle == Undefined.ERROR) return getItemFnEle;
            SplMethod getItemFn =
                    callEnv.getMemory().get((Reference) getItemFnEle);
            return getItemFn.call(EvaluatedArguments.of(objPtr, indexEle), callEnv, lineFile);
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
        return callObj + " " + args + (initialValue == null ? null : initialValue);
    }
}
