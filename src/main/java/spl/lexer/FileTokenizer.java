package spl.lexer;

import spl.lexer.tokens.IdToken;
import spl.lexer.tokens.Token;
import spl.lexer.treeList.BraceList;
import spl.lexer.treeList.CollectiveElement;
import spl.util.LineFilePos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class FileTokenizer extends Tokenizer {

    private static final String IMPORT_USAGE = "Usage of import: 'import \"path\"' or 'import \"path\" as <module>' " +
            "or 'import namespace \"path\"'";

    private final File srcFile;
    private final boolean importLang;

    public FileTokenizer(File srcFile, boolean importLang) {
        this.srcFile = srcFile;
        this.importLang = importLang;
    }

    private static BraceList makeTreeList(List<Token> tokenList) {
        BraceList root = new BraceList(null, LineFilePos.LF_TOKENIZER);
        CollectiveElement currentActive = root;
        for (int i = 0; i < tokenList.size(); ++i) {
            currentActive = makeTreeListRec(currentActive, tokenList, i);
        }
        return root;
    }

    public TokenizeResult tokenize() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(srcFile));

        tokens.clear();

        if (importLang) {
            LineFilePos.LineFile lineFile0 = new LineFilePos.LineFile(0, srcFile);
            tokens.add(new IdToken("import", new LineFilePos(lineFile0, 0)));
            tokens.add(new IdToken("namespace", new LineFilePos(lineFile0, 7)));
            tokens.add(new IdToken("lang", new LineFilePos(lineFile0, 17)));
            tokens.add(new IdToken(";", new LineFilePos(lineFile0, 21)));
        }

        int lineNum = 1;
        String line;
        while ((line = br.readLine()) != null) {
            LineFilePos.LineFile lineFile = new LineFilePos.LineFile(lineNum, srcFile);
            proceedLine(line, lineFile);
            lineNum++;
        }
        br.close();
        return new TokenizeResult(makeTreeList(tokens));
    }
}
