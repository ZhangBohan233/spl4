package ast;

import interpreter.env.Environment;
import interpreter.primitives.SplElement;
import util.LineFile;

public class CastExpr extends BinaryExpr {

    public CastExpr(LineFile lineFile) {
        super("as", lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
//        if (!(right instanceof TypeRepresent)) throw new SplException("Cast must be a type. ", getLineFile());
//        Type dstType = ((TypeRepresent) right).evalType(env);
//        SplElement srcTv = left.evaluate(env);
//
//        if (SplElement.isPrimitive(srcTv) && dstType.isPrimitive()) {
//            // primitive casts
//            SplElement value = srcTv.getValue();
//            PrimitiveType priDstT = (PrimitiveType) dstType;
//            switch (priDstT.type) {
//                case SplElement.INT:
//                    return new TypeValue(dstType, new Int(value.intValue()));
//                case SplElement.CHAR:
//                    return new TypeValue(dstType, new Char(value.charValue()));
//                case SplElement.FLOAT:
//                    return new TypeValue(dstType, new SplFloat(value.floatValue()));
//                // note that 'boolean' is not a case since boolean is not castable
//                default:
//                    throw new TypeError("Cannot cast primitive type '" + srcTv.getType() + "' to ' " +
//                            dstType + "'. ", getLineFile());
//            }
//
//        } else if (!srcTv.getType().isSuperclassOfOrEquals(dstType, env)) {
//            throw new SplException("Cannot cast type '" + srcTv.getType() + "' to' " + dstType + "'. ",
//                    getLineFile());
//        }
//        return new TypeValue(dstType, srcTv.getValue());
        return null;
    }

//    @Override
//    protected Type inferredType(Environment env) {
//        if (!(right instanceof TypeRepresent)) throw new SplException("Cast must be a type. ", getLineFile());
//        return ((TypeRepresent) right).evalType(env);
//    }

}
