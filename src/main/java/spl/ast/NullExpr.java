package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Pointer;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFile;

public class NullExpr extends LiteralNode {

    public NullExpr(LineFile lineFile) {
        super(lineFile);
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return Pointer.NULL_PTR;
    }

}
