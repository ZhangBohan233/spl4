package spl.tools.codeArea;

import javafx.scene.text.Font;

import java.util.List;

public class EmptyCodeAnalyzer extends CodeAnalyzer {

    public EmptyCodeAnalyzer(CodeArea codeArea, Font baseFont) {
        super(codeArea, baseFont);
    }

    @Override
    public void markKeyword(List<CodeArea.Text> line) {

    }
}
