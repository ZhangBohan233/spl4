package ast;

import interpreter.SplException;
import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.splObjects.SplArray;
import interpreter.types.*;
import util.LineFile;

import java.util.List;

public class IndexingNode extends Node  {

    private final Node callObj;
    private Line args;

    public IndexingNode(Node callObj, LineFile lineFile) {
        super(lineFile);

        this.callObj = callObj;
    }

    public void setArgs(Line args) {
        this.args = args;
    }

    public Line getArgs() {
        return args;
    }

    public Node getCallObj() {
        return callObj;
    }

//    public Type evalAtomType(Environment env) {
//        if (callObj instanceof IndexingNode) {
//            return ((IndexingNode) callObj).evalAtomType(env);
//        } else if (callObj instanceof TypeRepresent) {
//            return ((TypeRepresent) callObj).evalType(env);
//        } else {
//            throw new SplException("Not a type. ", getLineFile());
//        }
//    }

    @Override
    protected SplElement internalEval(Environment env) {

        SplElement callRes = getCallObj().evaluate(env);
        List<Node> arguments = getArgs().getChildren();
        int index = getIndex((Pointer) callRes, arguments, env, getLineFile());

//        Type arrEleType = ((ArrayType) callRes.getType()).getEleType();

        SplElement value = SplArray.getItemAtIndex((Pointer) callRes, index, env, getLineFile());
        return value;
    }
    @Override
    public String toString() {
        return callObj + " " + args;
    }

//    @Override
//    public Type evalType(Environment environment) {
//        Type ofType = ((TypeRepresent) callObj).evalType(environment);
//        return new ArrayType(ofType);
//    }

    public static int getIndex(Pointer arrayPtr, List<Node> arguments, Environment env, LineFile lineFile) {
//        if (!(arrayTv.getType() instanceof ArrayType)) {
//            throw new TypeError("Only array type supports indexing. ", lineFile);
//        }
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
