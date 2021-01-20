package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Char;
import spl.interpreter.primitives.SplElement;
import spl.util.BytesIn;
import spl.util.BytesOut;
import spl.util.LineFilePos;
import spl.util.Reconstructor;

import java.io.IOException;
import java.io.OutputStream;

public class CharLiteral extends LiteralNode {

    public final char ch;

    public CharLiteral(char ch, LineFilePos lineFile) {
        super(lineFile);

        this.ch = ch;
    }

    @Override
    protected SplElement internalEval(Environment env) {
        return new Char(ch);
    }

    @Override
    public String toString() {
        return "Char(" + ch + ')';
    }

    @Override
    protected void internalSave(BytesOut out) throws IOException {
        out.writeChar(ch);
    }

    public static CharLiteral reconstruct(BytesIn is, LineFilePos lineFilePos) throws Exception {
        return new CharLiteral(is.readChar(), lineFilePos);
    }

    public char getValue() {
        return ch;
    }
}
