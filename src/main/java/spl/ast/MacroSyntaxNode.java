package spl.ast;

import spl.interpreter.env.Environment;
import spl.lexer.treeList.BraceList;
import spl.lexer.treeList.BracketList;
import spl.util.LineFile;

public class MacroSyntaxNode extends Statement {

    private final BracketList syntaxList;
    private final BraceList bodyList;

    public MacroSyntaxNode(BracketList syntaxList, BraceList bodyList, LineFile lineFile) {
        super(lineFile);

        this.syntaxList = syntaxList;
        this.bodyList = bodyList;
    }

    @Override
    protected void internalProcess(Environment env) {

    }

    @Override
    public String toString() {
        return "syntax " + syntaxList + bodyList;
    }
}
