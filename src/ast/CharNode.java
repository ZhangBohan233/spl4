package ast;

import interpreter.env.Environment;
import interpreter.primitives.Char;
import interpreter.primitives.SplElement;
import util.LineFile;

public class CharNode extends LiteralNode {

    public final char ch;

    public CharNode(char ch, LineFile lineFile) {
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
