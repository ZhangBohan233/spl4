package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.io.OutputStream;

public class StringLiteralRef extends LiteralNode {

    private final StringLiteral literal;

    public StringLiteralRef(StringLiteral literal, LineFilePos lineFile) {
        super(lineFile);

        this.literal = literal;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return literal.evalRef(env, lineFile);
    }

    @Override
    public String toString() {
        return "Ref" + literal.toString();
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeString(new String(literal.getCharArray()));
    }

    public static StringLiteralRef reconstruct(BytesIn in, LineFilePos lineFilePos) throws Exception {
        String lit = in.readString();
        StringLiteral literal = in.literalMap.get(lit);
        if (literal == null) {
            literal = new StringLiteral(lit.toCharArray(), lineFilePos);
            in.literalMap.put(lit, literal);
        }
        return new StringLiteralRef(literal, lineFilePos);
    }
}
