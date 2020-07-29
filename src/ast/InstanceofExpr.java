package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class InstanceofExpr extends BinaryExpr {

    public InstanceofExpr(LineFile lineFile) {
        super("instanceof", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
//        if (!(right instanceof TypeRepresent))
//            throw new SplException("Right side of 'instanceof' must be a type. ", getLineFile());
        return null;
//        Type expectedT = ((TypeRepresent) right).evalType(env);
//        TypeValue leftTv = left.evaluate(env);
//
//        if (leftTv.getType().isPrimitive()) {
//            throw new TypeError("Primitive types do not support operation 'instanceof'. ", getLineFile());
//        }
//
//        SplObject obj = env.getMemory().get((Pointer) leftTv.getValue());
//        if (!(obj instanceof Instance)) return TypeValue.BOOL_FALSE;
//
//        Instance ins = (Instance) obj;
//        return Bool.boolValueOf(expectedT.isSuperclassOfOrEquals(ins.getType(), env));
    }
}
