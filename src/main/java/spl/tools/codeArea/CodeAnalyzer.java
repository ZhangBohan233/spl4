package spl.tools.codeArea;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.util.List;

public abstract class CodeAnalyzer {

    protected Paint keywordPaint;
    protected Font keywordFont;
    protected Paint codePaint;
    protected Font codeFont;

    public CodeAnalyzer(Paint keywordPaint, Font keywordFont, Paint codePaint, Font codeFont) {
        this.keywordPaint = keywordPaint;
        this.keywordFont = keywordFont;
        this.codePaint = codePaint;
        this.codeFont = codeFont;
    }

    public abstract void markKeyword(List<CodeArea.Text> line);

    public abstract void markVariable(List<CodeArea.Text> line);
}
