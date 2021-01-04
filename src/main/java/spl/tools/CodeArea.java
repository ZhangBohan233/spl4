package spl.tools;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CodeArea extends ScrollPane {

    public static final long FLASH_TIME = 500;

    private final static Paint background = Paint.valueOf("white");
    private final static Paint red = Paint.valueOf("red");
    private final static Paint black = Paint.valueOf("black");
    private final static Paint gold = Paint.valueOf("gold");
    private final static Paint lineBackground = Paint.valueOf("#DDDDDD");
    private final TextEditor textEditor = new TextEditor();
    private final double leftMargin = 5.0;
    private final GraphicsContext graphicsContext;
    private final ReadOnlyIntegerWrapper caretRow = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper caretCol = new ReadOnlyIntegerWrapper();
    @FXML
    Canvas canvas;
    @FXML
    Canvas lineCanvas;

    private double lineHeight = 16.0;
    private double charWidth = 8.0;
    private Timer timer;
    private FlashCaretTask fct;

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

        refresh();
    }

    public String getText() {
        return textEditor.getText();
    }

    public void setText(String text) {
        textEditor.setText(text);
        refresh();
    }

    public void clearCanvas() {
        graphicsContext.setFill(background);
        graphicsContext.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
    }

    public synchronized void refresh() {
        clearCanvas();
        graphicsContext.setFont(new Font("System Default", 12));
        int lineCount = textEditor.linesCount();
        canvas.setHeight(lineCount * lineHeight);
        lineCanvas.setHeight(Math.max((lineCount + 1) * lineHeight, this.getPrefHeight()));

        GraphicsContext lineGc = lineCanvas.getGraphicsContext2D();
        lineGc.setFill(lineBackground);
        lineGc.fillRect(0.0, 0.0, lineCanvas.getWidth(), lineCanvas.getHeight());
        lineGc.setFill(black);

        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            List<Text> line = textEditor.getLine(lineIndex);
            double y = getPosFromLineIndex(lineIndex);
            double widthUsed = leftMargin;
            double realY = y + lineHeight / 1.5;
            for (Text text : line) {
                graphicsContext.setFill(text.paint);
                graphicsContext.fillText(String.valueOf(text.text), widthUsed, realY);
                widthUsed += getCharWidth(text.text);
            }
            lineGc.fillText(String.valueOf(lineIndex + 1), leftMargin, realY);
        }
    }

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

    public void close() {
        stopTimer();
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
//                scrollDownToCaret();
//                double caretY = getPosFromLineIndex(newValue.intValue());
//                double caretPercent = (caretY) / getPrefHeight();
//                int screenLines = (int) (getPrefHeight() / lineHeight);
//                System.out.println(screenLines);
//                if (newValue.intValue() + 3 > screenLines) {
//                    setVvalue(1.0);
//                }
            }
        }));
        caretCol.getReadOnlyProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() != oldValue.intValue())
                refreshCaretFlasher();
        }));
    }

    private void refreshCaretFlasher() {
        stopTimer();
        startTimer();
    }

    private void stopTimer() {
        if (timer != null) {
            fct.clearCaret();
            timer.cancel();
            fct = null;
            timer = null;
        }
    }

    private void startTimer() {
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
        }
    }

    private void keyTypedHandler(KeyEvent keyEvent) {
        String chars = keyEvent.getCharacter();
        if (chars.isEmpty()) return;
        char first = chars.charAt(0);
        if (first == '\r' || first == '\n') {
            textEditor.newLine();
        } else if (first == '\b') {
            textEditor.backspace();
        } else {
            textEditor.typeText(chars);
        }
        refresh();
        scrollDownToCaret();
        scrollUpToCaret();
    }

    private void backgroundMouseClickedHandler(MouseEvent mouseEvent) {
        canvas.requestFocus();
    }

    private void mouseClickedHandler(MouseEvent mouseEvent) {
        canvas.requestFocus();
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

    private int getLineIndexOfPos(double y) {
        return (int) (y / lineHeight);
    }

    private double getPosFromLineIndex(int lineIndex) {
        return lineIndex * lineHeight;
    }

    private double getCharWidth(char c) {
        if (c >= 'A' && c <= 'Z') return charWidth * 1.25;
        return (c & 0xffff) >= 128 ? charWidth * 1.5 : charWidth;
    }

    private static class Text {
        private final char text;
        private Paint paint = black;

        Text(char text) {
            this.text = text;
        }
    }

    private class TextEditor {
        private final List<List<Text>> lines = new ArrayList<>();

        TextEditor() {
            lines.add(new ArrayList<>());
        }

        public String getText() {
            StringBuilder builder = new StringBuilder();
            for (List<Text> line : lines) {
                for (Text text : line) {
                    builder.append(text.text);
                }
                builder.append("\n");
            }
            return builder.toString();
        }

        public void setText(String text) {
            lines.clear();
            List<Text> activeLine = new ArrayList<>();
            for (char c : text.toCharArray()) {
                if (c == '\n') {
                    lines.add(activeLine);
                    activeLine = new ArrayList<>();
                } else {
                    activeLine.add(new Text(c));
                }
            }
        }

        public void typeText(String newText) {
            List<Text> line = lines.get(caretRow.get());
            for (char c : newText.toCharArray()) {
                Text t = new Text(c);
                line.add(caretCol.get(), t);
                caretCol.set(caretCol.get() + 1);
            }
        }

        public void backspace() {
            if (caretCol.get() == 0) {
                if (caretRow.get() > 0) {
                    List<Text> removed = lines.remove(caretRow.get());
                    caretRow.set(caretRow.get() - 1);
                    caretCol.set(lines.get(caretRow.get()).size());
                    getLine(caretRow.get()).addAll(removed);
                }
            } else {
                caretCol.set(caretCol.get() - 1);
                lines.get(caretRow.intValue()).remove(caretCol.get());
            }
        }

        public void newLine() {
            int newIndex = caretRow.get() + 1;
            List<Text> oldLine = getLine(caretRow.get());

            List<Text> newLine = new ArrayList<>(oldLine.subList(caretCol.get(), oldLine.size()));
            List<Text> oldLineTrimmed = new ArrayList<>(oldLine.subList(0, caretCol.get()));
            lines.set(caretRow.get(), oldLineTrimmed);
            lines.add(newIndex, newLine);

            caretRow.set(caretRow.get() + 1);
            caretCol.set(0);
        }

        int lineSize(int lineIndex) {
            return lines.get(lineIndex).size();
        }

        int linesCount() {
            return lines.size();
        }

        public List<List<Text>> getLines() {
            return lines;
        }

        List<Text> getLine(int lineIndex) {
            return lines.get(lineIndex);
        }

        private int getColOfIndex(int lineIndex, double x) {
            List<Text> line = lines.get(lineIndex);
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
            List<Text> line = lines.get(lineIndex);
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

        @Override
        public void run() {
            if (showing) {
                drawCaret(showingY, showingX, background);  // erase
            } else {
                double y = getPosFromLineIndex(caretRow.get());
                double x;
                if (caretCol.get() == 0) x = leftMargin;
                else x = textEditor.getXofCol(caretRow.get(), caretCol.get());

                drawCaret(y, x, black);
                showingY = y;
                showingX = x;
            }
            showing = !showing;
        }

        synchronized void clearCaret() {
            if (showing) {
                drawCaret(showingY, showingX, background);
                showing = false;
            }
        }

        private synchronized void drawCaret(double y, double x, Paint paint) {
            graphicsContext.setStroke(paint);
            graphicsContext.setLineWidth(1.0);
            graphicsContext.strokeLine(x, y, x, y + lineHeight);
        }
    }
}
