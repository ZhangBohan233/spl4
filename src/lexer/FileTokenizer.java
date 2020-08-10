package lexer;

import lexer.treeList.*;
import util.LineFile;
import util.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileTokenizer extends Tokenizer {

    private static final String IMPORT_USAGE = "Usage of import: 'import \"path\"' or 'import \"path\" as <module>' " +
            "or 'import namespace \"path\"'";

    private final File srcFile;
    private boolean main;
    private final boolean importLang;

    public FileTokenizer(File srcFile, boolean main, boolean importLang) {
        this.srcFile = srcFile;
        this.main = main;
        this.importLang = importLang;
    }

    public BraceList tokenize() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(srcFile));

        tokens.clear();

        if (importLang) {
            LineFile lineFile0 = new LineFile(0, srcFile);
            tokens.add(new IdToken("import", lineFile0));
            tokens.add(new IdToken("namespace", lineFile0));
            tokens.add(new IdToken("lang", lineFile0));
            tokens.add(new IdToken(";", lineFile0));
        }

        int lineNum = 1;
        String line;
        while ((line = br.readLine()) != null) {
            LineFile lineFile = new LineFile(lineNum, srcFile);
            proceedLine(line, lineFile);
            lineNum++;
        }
        return makeTreeList(tokens);
    }

    private static BraceList makeTreeList(List<Token> tokenList) {
        BraceList root = new BraceList(null, LineFile.LF_TOKENIZER);
        CollectiveElement currentActive = root;
        for (int i = 0; i < tokenList.size(); ++i) {
            currentActive = makeTreeListRec(currentActive, tokenList, i);
        }
        return root;
    }
}
