package spl.ast;

import spl.interpreter.env.Environment;
import spl.interpreter.primitives.Char;
import spl.interpreter.primitives.SplElement;
import spl.util.LineFilePos;

public class CharNode extends LiteralNode {

    public final char ch;

    public CharNode(char ch, LineFilePos lineFile) {
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
}
