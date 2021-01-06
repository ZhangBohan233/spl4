package spl.tools.codeArea;

import javafx.scene.text.Font;

public class EmptyCodeAnalyzer extends CodeAnalyzer {

    public EmptyCodeAnalyzer(CodeArea codeArea, Font baseFont) {
        super(codeArea, baseFont);
    }

    @Override
    public void markKeyword(CodeArea.TextLine line) {

    }
}
