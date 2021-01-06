package spl.tools.codeArea;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.*;

public class CodeArea extends ScrollPane {

    public static final long FLASH_TIME = 500;
    private final static Paint background = Paint.valueOf("white");
    private final static Paint red = Paint.valueOf("red");
    private final static Paint CODE = Paint.valueOf("black");
    private final static Paint CARET = Paint.valueOf("black");
    private final static Paint lineBackground = Paint.valueOf("#DDDDDD");
    private final static Paint HIGHLIGHT = Paint.valueOf("gray");
    private final TextEditor textEditor = new TextEditor();
    private final double leftMargin = 5.0;
    private final GraphicsContext graphicsContext;
    private final ReadOnlyIntegerWrapper caretRow = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper caretCol = new ReadOnlyIntegerWrapper();
    protected CodeAnalyzer codeAnalyzer;
    protected CodePref codePref = new CodePref();
    @FXML
    Canvas canvas;
    @FXML
    Canvas lineCanvas;
    private Font codeFont = Font.font("Lucida Console", 12.0);
    private Font keywordFont = Font.font("Lucida Console", 12.0);
    private double lineHeight = 15.6;
    private double charWidth = 7.8;
    private Timer timer;
    private FlashCaretTask fct;

    private int lastDragRow = -1;
    private int lastDragCol = -1;
    private int mousePressedRow = -1;
    private int mousePressedCol = -1;

    public CodeArea() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/spl/fxml/codeArea.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate view", e);
        }

        codeAnalyzer = new EmptyCodeAnalyzer(this, codeFont);

        canvas.setHeight(lineHeight);
        canvas.setCursor(Cursor.TEXT);
        this.setCursor(Cursor.TEXT);

        addFocusListener();
        addCaretPositionListener();

        graphicsContext = canvas.getGraphicsContext2D();

        clearCanvas();

        canvas.setOnKeyPressed(this::keyPressedHandler);
        canvas.setOnKeyTyped(this::keyTypedHandler);
        canvas.setOnMouseClicked(this::mouseClickedHandler);
        this.setOnMouseClicked(this::backgroundMouseClickedHandler);
        canvas.setOnMousePressed(this::mousePressHandler);
        canvas.setOnMouseDragged(this::mouseDragHandler);

        refresh();
    }

    public String getText() {
        return textEditor.getText();
    }

    public void setText(String text) {
        textEditor.setText(text);
        refresh();
    }

    public void setCodeFile(CodeFile codeFile) {
        this.codeAnalyzer.setCodeFile(codeFile);
    }

    public CodeAnalyzer getCodeAnalyzer() {
        return codeAnalyzer;
    }

    public void setCodeAnalyzer(CodeAnalyzer codeAnalyzer) {
        this.codeAnalyzer = codeAnalyzer;
    }

    public void setFont(Font font) {
        this.codeFont = font;
        this.charWidth = font.getSize() * 0.65;
        this.lineHeight = this.charWidth * 2;
    }

    public Font getCodeFont() {
        return codeFont;
    }

    public void setCodeFont(Font codeFont) {
        this.codeFont = codeFont;
    }

    public void setCodePref(CodePref codePref) {
        this.codePref = codePref;
    }

    @Override
    public void setHeight(double newHeight) {
        super.setHeight(newHeight);
        refresh();
    }

    public synchronized void clearCanvas() {
        graphicsContext.setFill(background);
        graphicsContext.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
    }

    public synchronized void refresh() {
        try {
            clearCanvas();
            int lineCount = textEditor.linesCount();
            canvas.setHeight(lineCount * lineHeight);
            lineCanvas.setHeight(Math.max((lineCount + 1) * lineHeight, this.getHeight()));

            GraphicsContext lineGc = lineCanvas.getGraphicsContext2D();
            lineGc.setFill(lineBackground);
            lineGc.fillRect(0.0, 0.0, lineCanvas.getWidth(), lineCanvas.getHeight());
            lineGc.setFill(CODE);

            for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
                TextLine line = textEditor.getLine(lineIndex);
                double y = getPosFromLineIndex(lineIndex);
                double widthUsed = leftMargin;
                double realY = y + lineHeight / 1.5;
                for (Text text : line) {
                    double cw = getCharWidth(text.text);
                    if (widthUsed + cw > canvas.getWidth()) {
                        canvas.setWidth(widthUsed + cw);
                    }
                    if (text.highlight || line.wholeLineHighLighted) {
                        graphicsContext.setFill(HIGHLIGHT);
                        graphicsContext.fillRect(widthUsed, y, cw, lineHeight);
                    }

                    graphicsContext.setFill(text.paint);
                    graphicsContext.setFont(text.font);
                    graphicsContext.fillText(String.valueOf(text.text), widthUsed, realY);
                    widthUsed += cw;
                }
                lineGc.fillText(String.valueOf(lineIndex + 1), leftMargin, realY);
            }
            if (fct != null) {
                fct.showCaretNow();
            }
        } catch (ClassCastException | InternalError e) {
            e.printStackTrace();
        }
    }

    public void close() {
        stopTimer();
        codeAnalyzer.close();
    }

    public TextEditor getTextEditor() {
        return textEditor;
    }

    public void setKeywordFont(Font keywordFont) {
        this.keywordFont = keywordFont;
    }

    /* listeners and handlers */

    private void scrollDownToCaret() {
        int screenRows = getLinesInScreen();
        int curBotRow = getCurrentTopLine() + screenRows - 3;
        if (caretRow.get() >= curBotRow) {
            int reqTopRow = caretRow.get() - screenRows + 2;
            scrollToLine(reqTopRow);
        }
    }

    private void scrollUpToCaret() {
        int curTopRow = getCurrentTopLine();
        if (caretRow.get() < curTopRow) {
            scrollToLine(caretRow.get() - 1);
        }
    }

    private int getCurrentTopLine() {
        int screenRows = getLinesInScreen();
        if (textEditor.linesCount() <= screenRows) return 0;
        int scrollableLines = (textEditor.linesCount() - screenRows);
        return (int) (this.getVvalue() * scrollableLines) + 2;  // +1 for (int) round down, +1 for the line in screen
    }

    private void scrollToLine(int topLineIndex) {
        int screenLines = getLinesInScreen();
        if (textEditor.linesCount() <= screenLines) {
            this.setVvalue(0.0);
            return;
        }
        double reqV = (double) topLineIndex / (textEditor.linesCount() - screenLines);
        this.setVvalue(reqV);
    }

    private int getLinesInScreen() {
        return (int) (getHeight() / lineHeight);
    }

    private void addFocusListener() {
        canvas.focusedProperty().addListener(this::focusListener);
        this.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                canvas.requestFocus();
            }
        }));
    }

    private void focusListener(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue && !oldValue) {
            startTimer();
        } else if (!newValue && oldValue) {
            stopTimer();
        }
    }

    private void addCaretPositionListener() {
        caretRow.getReadOnlyProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() != oldValue.intValue()) {
                refreshCaretFlasher();
            }
        }));
        caretCol.getReadOnlyProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() != oldValue.intValue())
                refreshCaretFlasher();
        }));
    }

    private synchronized void refreshCaretFlasher() {
        fct.showCaretNow();
    }

    private synchronized void stopTimer() {
        if (timer != null) {
            timer.cancel();
            fct.clearCaret();
            fct = null;
            timer = null;
        }
    }

    private synchronized void startTimer() {
        timer = new Timer();
        fct = new FlashCaretTask();
        timer.schedule(fct, 0, FLASH_TIME);
    }

    private void keyPressedHandler(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();
        if (keyCode == KeyCode.LEFT) {
            if (caretCol.get() > 0) caretCol.set(caretCol.get() - 1);
        } else if (keyCode == KeyCode.RIGHT) {
            if (caretCol.get() < textEditor.lineSize(caretRow.get())) caretCol.set(caretCol.get() + 1);
        } else if (keyCode == KeyCode.UP) {
            keyEvent.consume();
            if (caretRow.get() > 0) {
                caretRow.set(caretRow.get() - 1);
                int upLineSize = textEditor.lineSize(caretRow.get());
                if (caretCol.get() > upLineSize) {
                    caretCol.set(upLineSize);
                }
                scrollUpToCaret();
            }
        } else if (keyCode == KeyCode.DOWN) {
            keyEvent.consume();
            if (caretRow.get() < textEditor.linesCount() - 1) {
                caretRow.set(caretRow.get() + 1);
                int downLineSize = textEditor.lineSize(caretRow.get());
                if (caretCol.get() > downLineSize) {
                    caretCol.set(downLineSize);
                }
                scrollDownToCaret();
            }
        } else if (keyCode == KeyCode.TAB) {
            keyEvent.consume();
        } else if (keyCode == KeyCode.SPACE) {
            keyEvent.consume();
        }
    }

    private void keyTypedHandler(KeyEvent keyEvent) {
        String chars = keyEvent.getCharacter();
        keyEvent.consume();
        if (chars.isEmpty()) return;
        keyTyped(chars);

        refresh();
        scrollDownToCaret();
        scrollUpToCaret();
    }

    private void keyTyped(String text) {
        for (char c : text.toCharArray()) {
            keyTyped(c);
        }
    }

    private void keyTyped(char c) {
        switch (c) {
            case '\r':
            case '\n':
                textEditor.newLine();
                break;
            case '\b':
                textEditor.backspace();
                break;
            case ' ':
                textEditor.typeText(" ");
                break;
            case '\t':
                textEditor.typeText("    ");
                break;
            case 3:
                copyAction();
                break;
            case 22:
                pasteAction();
                break;
            case 24:
                cutAction();
                break;
            case 26:
                undoAction();
                break;
            default:
                textEditor.typeText(c);
                break;
        }
    }

    private void backgroundMouseClickedHandler(MouseEvent mouseEvent) {
        canvas.requestFocus();
    }

    private void mouseClickedHandler(MouseEvent mouseEvent) {
        canvas.requestFocus();
        if (mouseEvent.isStillSincePress()) {
            lastDragRow = -1;
            lastDragCol = -1;
            textEditor.clearHighlights();
            refresh();
        }
        if (textEditor.lines.isEmpty()) {
            caretRow.set(0);
            caretCol.set(0);
            return;
        }

        int lineIndex = getLineIndexOfPos(mouseEvent.getY());
        if (lineIndex >= textEditor.lines.size()) {
            lineIndex = textEditor.lines.size() - 1;
        }
        caretRow.set(lineIndex);
        caretCol.set(textEditor.getColOfIndex(lineIndex, mouseEvent.getX()));
    }

    private void mousePressHandler(MouseEvent mouseEvent) {
        int lineIndex = getLineIndexOfPos(mouseEvent.getY());
        if (lineIndex >= textEditor.lines.size()) {
            lineIndex = textEditor.lines.size() - 1;
        }
        mousePressedRow = lineIndex;
        mousePressedCol = textEditor.getColOfIndex(lineIndex, mouseEvent.getX());
    }

    private void mouseDragHandler(MouseEvent mouseEvent) {
        int lineIndex = getLineIndexOfPos(mouseEvent.getY());
        if (lineIndex >= textEditor.lines.size()) {
            lineIndex = textEditor.lines.size() - 1;
        }
        int col = textEditor.getColOfIndex(lineIndex, mouseEvent.getX());
        if (lineIndex != lastDragRow || col != lastDragCol) {
            lastDragRow = lineIndex;
            lastDragCol = col;
            textEditor.clearHighlights();
            markHighlights(lineIndex, col);
            refresh();
        }
    }

    private void markHighlights(int row, int col) {
        int startRow = mousePressedRow;
        int startCol = mousePressedCol;
        if (row == startRow) {
            if (startCol == col) return;
            textEditor.hasHighlight = true;
            int c1 = Math.min(startCol, col);
            int c2 = Math.max(startCol, col);
            TextLine line = textEditor.getLine(row);
            for (int c = c1; c < c2; c++) {
                line.get(c).highlight = true;
            }
        } else if (row > startRow) {  // drag to front
            markAcrossRows(startRow, startCol, row, col);
        } else {  // drag to back
            markAcrossRows(row, col, startRow, startCol);
        }
    }

    private void markAcrossRows(int row, int col, int startRow, int startCol) {
        textEditor.hasHighlight = true;
        TextLine startLine = textEditor.getLine(row);  // first line
        for (int i = col; i < startLine.size(); i++) {
            startLine.get(i).highlight = true;
        }
        for (int i = row + 1; i < startRow; i++) {  // middle lines
            textEditor.getLine(i).setWholeLineHighLighted(true);
        }
        TextLine endLine = textEditor.getLine(startRow);  // last line
        for (int i = 0; i < startCol; i++) {
            endLine.get(i).highlight = true;
        }
    }

    private void copyAction() {
        String content = textEditor.getHighlightedString();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent cc = new ClipboardContent();
        cc.putString(content);
        clipboard.setContent(cc);
    }

    private void pasteAction() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        String content = clipboard.getString();
        if (content != null) {
            for (char c : content.toCharArray()) {
                keyTyped(c);
            }
            refresh();
            scrollDownToCaret();
            scrollUpToCaret();
        }
    }

    private void cutAction() {
        copyAction();
        textEditor.deleteHighlighted();
    }

    private void undoAction() {

    }

    private int getLineIndexOfPos(double y) {
        return (int) (y / lineHeight);
    }

    private double getPosFromLineIndex(int lineIndex) {
        return lineIndex * lineHeight;
    }

    private double getCharWidth(char c) {
        return (c & 0xffff) >= 128 ? charWidth * 1.5 : charWidth;
    }

    public class Text {
        public final char text;
        private Paint paint = CODE;
        private Font font = codeFont;
        private boolean highlight = false;

        Text(char text) {
            this.text = text;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        @Override
        public String toString() {
            return String.valueOf(text);
        }
    }


    public static class TextLine extends ArrayList<Text> {

        /**
         * Is the whole line highlighted, including the eol.
         */
        private boolean wholeLineHighLighted = false;

        public TextLine() {
            super();
        }

        public TextLine(Collection<? extends Text> c) {
            super(c);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Text t : this) builder.append(t.text);
            return builder.toString();
        }

        void setWholeLineHighLighted(boolean v) {
            this.wholeLineHighLighted = v;
        }
    }

    public class TextEditor {
        private final List<TextLine> lines = new ArrayList<>();
        private boolean hasHighlight = false;

        TextEditor() {
            lines.add(new TextLine());
        }

        public String getText() {
            StringBuilder builder = new StringBuilder();
            for (TextLine line : lines) {
                for (Text text : line) {
                    builder.append(text.text);
                }
                builder.append("\n");
            }
            builder.setLength(builder.length() - 1);  // removes the last '\n'
            return builder.toString();
        }

        public void setText(String text) {
            lines.clear();
            TextLine activeLine = new TextLine();
            for (char c : text.toCharArray()) {
                if (c == '\n') {
                    lines.add(activeLine);
                    analyze(activeLine);
                    activeLine = new TextLine();
                } else {
                    activeLine.add(new Text(c));
                }
            }
            lines.add(activeLine);
            analyze(activeLine);
        }

        public void clearHighlights() {
            for (TextLine line : lines) {
                line.setWholeLineHighLighted(false);
                for (Text t : line) {
                    t.highlight = false;
                }
            }
            hasHighlight = false;
        }

        public String getHighlightedString() {
            StringBuilder builder = new StringBuilder();
            boolean eol = false;
            for (TextLine line : lines) {
                int lineSize = line.size();
                if (line.wholeLineHighLighted) {
                    if (eol) builder.append('\n');
                    builder.append(line.toString());
                    eol = true;
                } else {
                    for (int i = 0; i < lineSize; i++) {
                        Text t = line.get(i);
                        if (t.highlight) {
                            if (i == 0) {
                                if (eol) builder.append('\n');
                                eol = false;
                            }
                            builder.append(t.text);
                        }
                        if (i == lineSize - 1) eol = true;
                    }
                }
            }
            return builder.toString();
        }

        public void deleteHighlighted() {
            int row = 0;
            int startRow = -1;
            int endRow = -1;
            Iterator<TextLine> linesIterator = lines.listIterator();
            while (linesIterator.hasNext()) {
                TextLine line = linesIterator.next();
                if (line.wholeLineHighLighted) {
                    if (startRow == -1) {  // first encounter
                        startRow = row;
                        caretRow.set(row);
                        caretCol.set(0);
                    }
                    linesIterator.remove();
                    endRow = row;
                } else {
                    int i = 0;
                    while (i < line.size()) {
                        Text text = line.get(i);
                        if (text.highlight) {
                            if (startRow == -1) {
                                startRow = row;
                                caretRow.set(row);
                                caretCol.set(i);
                            }
                            line.remove(i);
                            endRow = row;
                        }
                        else i++;
                    }
                }
                row++;
            }
            if (startRow != endRow) {  // cross line deletion
                TextLine curLine = getLine(caretRow.get());
                TextLine nextLine = lines.remove(caretRow.get() + 1);
                curLine.addAll(nextLine);
            }
        }

        public void typeText(String newText) {
            if (hasHighlight) {
                deleteHighlighted();
                hasHighlight = false;
            }
            TextLine line = lines.get(caretRow.get());
            for (char c : newText.toCharArray()) {
                typeText(c, line);
            }
            analyze(line);
        }

        public void typeText(char c) {
            if (hasHighlight) {
                deleteHighlighted();
                hasHighlight = false;
            }
            TextLine line = lines.get(caretRow.get());
            typeText(c, line);
            analyze(line);
        }

        private void typeText(char c, TextLine line) {
            Text t = new Text(c);
            line.add(caretCol.get(), t);
            caretCol.set(caretCol.get() + 1);
        }

        public void backspace() {
            if (hasHighlight) {
                deleteHighlighted();
                hasHighlight = false;
                return;
            }
            if (caretCol.get() == 0) {
                if (caretRow.get() > 0) {
                    TextLine removed = lines.remove(caretRow.get());
                    caretRow.set(caretRow.get() - 1);
                    caretCol.set(lines.get(caretRow.get()).size());
                    getLine(caretRow.get()).addAll(removed);
                    analyze(getLine(caretRow.get()));
                }
            } else {
                caretCol.set(caretCol.get() - 1);
                getLine(caretRow.get()).remove(caretCol.get());
                analyze(getLine(caretRow.get()));
            }
        }

        public void newLine() {
            if (hasHighlight) {
                deleteHighlighted();
                hasHighlight = false;
            }
            int newIndex = caretRow.get() + 1;
            TextLine oldLine = getLine(caretRow.get());

            TextLine newLine = new TextLine(oldLine.subList(caretCol.get(), oldLine.size()));
            TextLine oldLineTrimmed = new TextLine(oldLine.subList(0, caretCol.get()));
            lines.set(caretRow.get(), oldLineTrimmed);
            lines.add(newIndex, newLine);

            caretRow.set(caretRow.get() + 1);
            caretCol.set(0);

            boolean indented = false;
            if (codePref.isAutoBackBrace()) {
                if (oldLineTrimmed.size() > 0) {
                    if (autoAddBack(newLine)) {
                        if (codePref.isAutoIndent()) {
                            /*
                            .... {
                                |
                            }
                            */
                            TextLine blankLine = new TextLine();
                            lines.add(caretRow.get(), blankLine);
                            autoIndent(blankLine);
                            indented = true;
                        }
                    }
                }
            }
            if (!indented && codePref.isAutoIndent()) {
                autoIndent(newLine);
            }

            analyze(newLine);
            analyze(oldLineTrimmed);
        }

        private void autoIndent(TextLine newLine) {
            int lastLineIndex = caretRow.get() - 1;
            int thisIndent = firstNonSpaceIndex(newLine);
            char nlFirstChar;
            if (thisIndent < 0) {
                thisIndent = 0;
                nlFirstChar = '\0';
            } else {
                nlFirstChar = newLine.get(thisIndent).text;
            }
            char lastChar = lastNonSpaceChar(lastLineIndex);
            int expectedIndent;
            int lastIndent = firstNonSpaceIndex(getLine(lastLineIndex));
            if (lastIndent < 0) {  // last line is empty
                expectedIndent = 0;
            } else if (lastChar == ';' || nlFirstChar == '}' || nlFirstChar == ')' || nlFirstChar == ']') {
                expectedIndent = lastIndent;
            } else if (lastChar == '{' || lastChar == '(' || lastChar == '[') {
                expectedIndent = lastIndent + 4;
            } else {
                expectedIndent = lastIndent;
//                expectedIndent = lastIndent + 8;
            }
            int indentNeeded = expectedIndent - thisIndent;
            TextLine insert = new TextLine();
            for (int i = 0; i < indentNeeded; i++) {
                insert.add(new Text(' '));
            }
            newLine.addAll(0, insert);
            caretCol.set(caretCol.get() + indentNeeded);
        }

        private boolean autoAddBack(TextLine newLine) {
            for (char[] fb : CodePref.FRONT_BACKS) {
                char front = fb[0];
                char back = fb[1];
                char last = lastNonSpaceChar(caretRow.get() - 1);
                if (last == front) {
                    if (nextNonSpaceChar(caretRow.get()) != back) {
                        int indexNewLine = firstNonSpaceIndex(newLine);
                        addBack(newLine, back, Math.max(indexNewLine, 0));
                        return true;
                    }
                }
            }
            return false;
        }

        private char nextNonSpaceChar(int row) {
            for (int i = row; i < linesCount(); i++) {
                TextLine line = getLine(i);
                for (Text t : line) {
                    if (t.text != ' ') return t.text;
                }
            }
            return '\0';
        }

        private char lastNonSpaceChar(int row) {
            for (int i = row; i >= 0; i--) {
                TextLine line = getLine(i);
                for (int j = line.size() - 1; j >= 0; j--) {
                    Text t = line.get(j);
                    if (t.text != ' ') return t.text;
                }
            }
            return '\0';
        }

        private void addBack(TextLine line, char toAdd, int firstNonSpaceIndex) {
            line.add(firstNonSpaceIndex, new Text(toAdd));
            caretCol.set(firstNonSpaceIndex);
        }

        private int firstNonSpaceIndex(TextLine line) {
            for (int i = 0; i < line.size(); i++) {
                if (line.get(i).text != ' ') return i;
            }
            return -1;
        }

        int lineSize(int lineIndex) {
            return lines.get(lineIndex).size();
        }

        int linesCount() {
            return lines.size();
        }

        public List<TextLine> getLines() {
            return lines;
        }

        TextLine getLine(int lineIndex) {
            return lines.get(lineIndex);
        }

        private void analyze(TextLine line) {
            codeAnalyzer.markKeyword(line);
        }

        private int getColOfIndex(int lineIndex, double x) {
            TextLine line = lines.get(lineIndex);
            double width = leftMargin;
            for (int i = 0; i < line.size(); i++) {
                Text t = line.get(i);

                double cw = getCharWidth(t.text);

                if (width + cw / 2 >= x) return i;
                else if (width - cw / 2 >= x) return i + 1;

                width += cw;
            }
            return line.size();
        }

        private double getXofCol(int lineIndex, int col) {
            TextLine line = lines.get(lineIndex);
            double width = leftMargin;
            for (int i = 0; i < col; i++) {
                if (i >= line.size()) return width;
                Text t = line.get(i);
                width += getCharWidth(t.text);
            }
            return width;
        }

        @Override
        public String toString() {
            return getText();
        }
    }

    private class FlashCaretTask extends TimerTask {
        private boolean showing;
        private double showingY;
        private double showingX;
        private boolean manualShow;

        @Override
        public void run() {
            runTask();
        }

        private synchronized void runTask() {
            if (showing) {
                if (manualShow) {
                    manualShow = false;
                    return;
                }
                hideCaret();  // erase
            } else {
                showCaret();
            }
            showing = !showing;
        }

        synchronized void clearCaret() {
            if (showing) {
                hideCaret();
                showing = false;
            }
        }

        synchronized void showCaretNow() {
            if (!showing) {
                showCaret();
                showing = true;
                manualShow = true;
            } else {
                hideCaret();
                showCaret();
            }
        }

        private synchronized void hideCaret() {
            drawCaret(showingY, showingX, background);
        }

        private synchronized void showCaret() {
            double y = getPosFromLineIndex(caretRow.get());
            double x;
            if (caretCol.get() == 0) x = leftMargin;
            else x = textEditor.getXofCol(caretRow.get(), caretCol.get());

            drawCaret(y, x, CARET);
            showingY = y;
            showingX = x;
        }

        private synchronized void drawCaret(double y, double x, Paint paint) {
            try {
                drawVerticalLine(y, x - 1, paint);
            } catch (InternalError | ClassCastException e) {
                e.printStackTrace();
            }
        }

        private synchronized void drawVerticalLine(double y, double x, Paint paint) {
            PixelWriter pw = graphicsContext.getPixelWriter();
            Color color = (Color) paint;

            int y0 = (int) Math.round(y);
            int y1 = (int) Math.round(y + lineHeight);
            int xx = (int) Math.round(x);

            for (int yy = y0; yy < y1; yy++) {
                pw.setColor(xx, yy, color);
            }
        }
    }
}
