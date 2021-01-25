package spl.ast;

import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.primitives.Undefined;
import spl.interpreter.splObjects.Instance;
import spl.interpreter.splObjects.SplCallable;
import spl.lexer.SyntaxError;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.Constants;
import spl.util.LineFilePos;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DictSetLiteral extends Expression {

    private final List<Node> nodes;
    private final boolean isDict;

    private DictSetLiteral(List<Node> nodes, boolean isDict, LineFilePos lineFile) {
        super(lineFile);

        this.nodes = nodes;
        this.isDict = isDict;
    }

    public static DictSetLiteral create(Line line, LineFilePos lineFilePos) {
        int type = 0;  // 0: uninitialized, 1: dict, 2: set
        for (Node part : line.getChildren()) {
            if (part instanceof Assignment) {
                if (type == 2) throw new SyntaxError("Cannot determine dict or set creation.", lineFilePos);
                type = 1;
            } else if (part instanceof Expression) {
                if (type == 1) throw new SyntaxError("Cannot determine dict or set creation.", lineFilePos);
                type = 2;
            } else {
                throw new SyntaxError("Elements of dict or set must be expressions.", lineFilePos);
            }
        }
        return new DictSetLiteral(line.getChildren(), type != 2, lineFilePos);
    }

    public static DictSetLiteral reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        List<Node> nodes = is.readList();
        boolean isDict = is.readBoolean();
        return new DictSetLiteral(nodes, isDict, lineFilePos);
    }

    public static SplElement javaMapToSplMap(Map<String, SplElement> map, Environment env, LineFilePos lineFilePos) {
        Instance.InstanceAndPtr iap = Instance.createInstanceWithInitCall(
                Constants.HASH_DICT, EvaluatedArguments.of(), env, lineFilePos);
        if (iap == null) return Undefined.ERROR;
        Reference putFnPtr = (Reference) iap.instance.getEnv().get(Constants.SET_ITEM_FN, lineFilePos);
        SplCallable putFn = env.getMemory().get(putFnPtr);

        for (Map.Entry<String, SplElement> entry : map.entrySet()) {
            SplElement key = StringLiteral.createString(entry.getKey().toCharArray(), env, lineFilePos);

            putFn.call(EvaluatedArguments.of(iap.pointer, key, entry.getValue()), env, lineFilePos);
        }
        return iap.pointer;
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeList(nodes);
        out.writeBoolean(isDict);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return isDict ? createDict(env) : createSet(env);
    }

    private SplElement createDict(Environment env) {
        Instance.InstanceAndPtr iap = Instance.createInstanceWithInitCall(
                Constants.HASH_DICT, EvaluatedArguments.of(), env, lineFile);
        if (iap == null) return Undefined.ERROR;
        Reference putFnPtr = (Reference) iap.instance.getEnv().get(Constants.SET_ITEM_FN, lineFile);
        SplCallable putFn = env.getMemory().get(putFnPtr);

        for (Node node : nodes) {
            Assignment ass = (Assignment) node;
            SplElement left = ass.getLeft().evaluate(env);
            if (left == Undefined.ERROR) return left;
            SplElement right = ass.getRight().evaluate(env);
            if (right == Undefined.ERROR) return right;

            putFn.call(EvaluatedArguments.of(iap.pointer, left, right), env, lineFile);
        }
        return iap.pointer;
    }

    private SplElement createSet(Environment env) {
        Instance.InstanceAndPtr iap = Instance.createInstanceWithInitCall(
                Constants.HASH_SET, EvaluatedArguments.of(), env, lineFile);
        if (iap == null) return Undefined.ERROR;
        Reference putFnPtr = (Reference) iap.instance.getEnv().get(Constants.PUT_FN, lineFile);
        SplCallable putFn = env.getMemory().get(putFnPtr);

        for (Node node : nodes) {
            SplElement value = node.evaluate(env);
            // guaranteed not null, since 'Expression' check happened while creation
            if (value == Undefined.ERROR) return Undefined.ERROR;

            putFn.call(EvaluatedArguments.of(iap.pointer, value), env, lineFile);
        }
        return iap.pointer;
    }

    public boolean isDict() {
        return isDict;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        if (isDict) return "Dict{" + nodes + "}";
        else return "Set{" + nodes + "}";
    }
}
