package ast;

import interpreter.env.Environment;
import interpreter.primitives.Pointer;
import interpreter.primitives.SplElement;
import interpreter.types.*;
import util.LineFile;

public class NullExpr extends LiteralNode {

    public NullExpr(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return Pointer.NULL_PTR;
    }

}
