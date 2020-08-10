package lexer;

import lexer.treeList.BraceList;
import lexer.treeList.BracketList;
import lexer.treeList.CollectiveElement;
import util.LineFile;

import java.util.List;

public class ConsoleTokenizer extends Tokenizer {

    public void addLine(String line) {
        proceedLine(line, LineFile.LF_CONSOLE);
    }

    public BracketList build() {
        BracketList rootList = makeOneStmtList(tokens);
        tokens.clear();
        return rootList;
    }

    public void clear() {
        tokens.clear();;
    }

    public boolean readyToBuild() {
        if (tokens.isEmpty()) return true;
        if (isClosedBlock()) {
            Token last = tokens.get(tokens.size() - 1);
            return last instanceof IdToken && ((IdToken) last).getIdentifier().equals(";");
        }
        return false;
    }

    private static BracketList makeOneStmtList(List<Token> tokenList) {
        BracketList root = new BracketList(null, LineFile.LF_TOKENIZER);
        CollectiveElement currentActive = root;
        for (int i = 0; i < tokenList.size(); ++i) {
            currentActive = makeTreeListRec(currentActive, tokenList, i);
        }
        return root;
    }

    private boolean isClosedBlock() {
        int parCount = 0;
        int sqrCount = 0;
        int braceCount = 0;
        for (Token tk : tokens) {
            if (tk instanceof IdToken) {
                String symbol = ((IdToken) tk).getIdentifier();
                switch (symbol) {
                    case "(" -> parCount++;
                    case ")" -> parCount--;
                    case "[" -> sqrCount++;
                    case "]" -> sqrCount--;
                    case "{" -> braceCount++;
                    case "}" -> braceCount--;
                }
            }
        }
        return parCount == 0 && sqrCount == 0 && braceCount == 0;
    }
}
