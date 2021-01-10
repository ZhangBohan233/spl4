package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Char;
import spl.interpreter.primitives.SplByte;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class ByteLiteral extends LiteralNode {

    public final byte b;

    public ByteLiteral(byte b, LineFilePos lineFile) {
        super(lineFile);

        this.b = b;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return new SplByte(b);
    }

    @Override
    public String toString() {
        return "Byte(" + b + ")";
    }
}

