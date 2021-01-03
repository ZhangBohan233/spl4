package spl.tools;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CodeArea extends ScrollPane {

    private final static Paint background = Paint.valueOf("white");
    private final static Paint red = Paint.valueOf("red");
    private final static Paint black = Paint.valueOf("black");
    private final static Paint gold = Paint.valueOf("gold");
    private final TextEditor textEditor = new TextEditor();
    private final double leftMargin = 5.0;
    @FXML
    Canvas canvas;
    private final GraphicsContext graphicsContext;
    private double lineHeight = 16.0;
    private double charWidth = 10.0;
    private Timer timer;
    private FlashCaretTask fct;
    private int caretRow;
    private int caretCol;

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

        canvas.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue && !oldValue) {
                fct = new FlashCaretTask();
                timer = new Timer();
                timer.schedule(fct, 500, 500);
            } else if (!newValue && oldValue) {
                fct.clearCaret();
                timer.cancel();
            }
        }));

        graphicsContext = canvas.getGraphicsContext2D();

        clearCanvas();
        canvas.setCursor(Cursor.TEXT);

        canvas.setOnKeyPressed(keyEvent -> {
            System.out.println(keyEvent.getCode());
        });
        canvas.setOnKeyTyped(keyEvent -> {
            String chars = keyEvent.getCharacter();
            if (chars.isEmpty()) return;
            char first = chars.charAt(0);
            if (first == '\r' || first == '\n') {
                System.out.println("newline");
                textEditor.newLine();
            } else {
                textEditor.typeText(chars);
            }
            refresh();
        });
        canvas.setOnMouseClicked(event -> {
            canvas.requestFocus();
            System.out.println("x: " + event.getX() + ", y " + event.getY());
            if (textEditor.lines.isEmpty()) {
                caretRow = 0;
                caretCol = 0;
                return;
            }

            int lineIndex = getLineIndexOfPos(event.getY());
            if (lineIndex >= textEditor.lines.size()) {
                lineIndex = textEditor.lines.size() - 1;
            }
            caretRow = lineIndex;
            caretCol = textEditor.getColOfIndex(lineIndex, event.getX());
            System.out.println(caretRow + " " + caretCol);
        });
//        canvas.requestFocus();
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
//        graphicsContext.setLineWidth(2.0);
        graphicsContext.setFont(new Font("System Default", 12));
        for (int lineIndex = 0; lineIndex < textEditor.lines.size(); lineIndex++) {
            List<Text> line = textEditor.lines.get(lineIndex);
            double y = getPosFromLineIndex(lineIndex);
            double widthUsed = leftMargin;
            for (Text text : line) {
                graphicsContext.setFill(text.paint);
                graphicsContext.fillText(String.valueOf(text.text), widthUsed, y + lineHeight / 1.5);
                widthUsed += getCharWidth(text.text);
            }
        }
    }

    public void close() {
        timer.cancel();
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
            List<Text> line = lines.get(caretRow);
            for (char c : newText.toCharArray()) {
                Text t = new Text(c);
                line.add(caretCol++, t);
            }
        }

        public void newLine() {
            lines.add(++caretRow, new ArrayList<>());
            caretCol = 0;
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
        private int showingRow;
        private int showingCol;

        @Override
        public void run() {
            if (showing) {
                drawCaret(showingRow, showingCol, background);  // erase
            } else {
                drawCaret(caretRow, caretCol, black);
                showingRow = caretRow;
                showingCol = caretCol;
            }
            showing = !showing;
        }

        synchronized void clearCaret() {
            if (showing) {
                drawCaret(showingRow, showingCol, background);
                showing = false;
            }
        }

        private synchronized void drawCaret(int row, int col, Paint paint) {
            graphicsContext.setStroke(paint);
            graphicsContext.setLineWidth(1.0);
            double y = getPosFromLineIndex(row);
            double x;
            if (col == 0) x = leftMargin;
            else x = textEditor.getXofCol(row, col);
            graphicsContext.strokeLine(x, y, x, y + lineHeight);
        }
    }
}
