package spl.tools.codeArea;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.io.File;
import java.util.List;

public abstract class CodeAnalyzer {

    private static final Paint basePaint = Paint.valueOf("black");
    protected CodeArea codeArea;
    protected Paint keywordPaint = Paint.valueOf("darkorange");
    protected Font keywordFont;
    protected Paint codePaint = basePaint;
    protected Font codeFont;
    protected Paint functionPaint = Paint.valueOf("olive");
    protected Paint stringPaint = Paint.valueOf("darkOliveGreen");
    protected Paint attributePaint = Paint.valueOf("indigo");
    protected Paint functionCallPaint = Paint.valueOf("blue");
    protected Paint numberPaint = Paint.valueOf("dodgerBlue");

    protected CodeFile codeFile;

    public CodeAnalyzer(CodeArea codeArea, Font baseFont) {
        this.codeArea = codeArea;
        this.codeFont = baseFont;
        this.keywordFont = baseFont;
    }

    public void setKeywordFont(Font keywordFont) {
        this.keywordFont = keywordFont;
    }

    public void setCodeFile(CodeFile codeFile) {
        this.codeFile = codeFile;
    }

    public abstract void markKeyword(List<CodeArea.Text> line);

    public abstract void markVariable(List<CodeArea.Text> line);

    public void close() {
    }
}
