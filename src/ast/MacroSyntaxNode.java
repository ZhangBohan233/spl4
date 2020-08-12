package ast;

import interpreter.env.Environment;
import lexer.treeList.BraceList;
import lexer.treeList.BracketList;
import util.LineFile;

public class MacroSyntaxNode extends AbstractStatement {

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
