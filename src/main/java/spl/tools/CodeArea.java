package spl.tools;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class CodeArea extends Pane {

    @FXML
    Canvas canvas;

    private GraphicsContext graphicsContext;

    private final static Paint background = Paint.valueOf("white");
    private final static Paint red = Paint.valueOf("red");
    private final static Paint black = Paint.valueOf("black");
    private final static Paint gold = Paint.valueOf("gold");

    private double lineHeight = 18.0;
    private double charWidth = 10.0;

    private Timer timer = new Timer();
    protected final TextEditor textEditor = new TextEditor();
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

        timer.schedule(new FlashCaretTask(), 600, 600);

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
        graphicsContext.setFont(new Font("微软雅黑", 15));
        for (int lineIndex = 0; lineIndex < textEditor.lines.size(); lineIndex++) {
            List<Text> line = textEditor.lines.get(lineIndex);
            double y = getPosFromLineIndex(lineIndex);
            double widthUsed = 0.0;
            for (Text text : line) {
                graphicsContext.setFill(text.paint);
                graphicsContext.fillText(String.valueOf(text.text), widthUsed, y);
                if (isDoubleWidthChar(text.text)) {
                    widthUsed += charWidth * 2;
                } else {
                    widthUsed += charWidth;
                }
            }
        }
    }

    public void close() {
        timer.cancel();
    }

    private int getLineIndexOfPos(double y) {
        return (int) (y % lineHeight);
    }

    private double getPosFromLineIndex(int lineIndex) {
        return lineIndex * lineHeight;
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
            lines.add(caretRow++, new ArrayList<>());
        }

        private int getColOfIndex(int lineIndex, double x) {
            List<Text> line = lines.get(lineIndex);
            double width = 0.0;
            for (int i = 0; i < line.size(); i++) {
                Text t = line.get(i);
                if (isDoubleWidthChar(t.text)) width += charWidth * 2;
                else width += charWidth;
                if (width >= x) return i;
            }
            return line.size();
        }

        private double getXofCol(int lineIndex, int col) {
            List<Text> line = lines.get(lineIndex);
            double width = 0.0;
            for (int i = 0; i < col; i++) {
                Text t = line.get(i);
                if (isDoubleWidthChar(t.text)) width += charWidth * 2;
                else width += charWidth;
            }
            return width;
        }

        @Override
        public String toString() {
            return getText();
        }
    }

    private static class Text {
        private final char text;
        private Paint paint = black;

        Text(char text) {
            this.text = text;
        }
    }

    private class FlashCaretTask extends TimerTask {
        private boolean showing;
        private int showingRow;
        private int showingCol;

        @Override
        public void run() {
//            if (!isFocused()) return;
            if (showing) {
                drawCaret(showingRow, showingCol, background);  // erase
            } else {
                drawCaret(caretRow, caretCol, black);
                showingRow = caretRow;
                showingCol = caretCol;
            }
            showing = !showing;
        }

        private synchronized void drawCaret(int row, int col, Paint paint) {
            graphicsContext.setStroke(paint);
            graphicsContext.setLineWidth(2.0);
            double y = getPosFromLineIndex(row);
            double x;
            if (col == 0) x = 0;
            else x = textEditor.getXofCol(row, col);
            graphicsContext.strokeLine(x, y, x, y + lineHeight);
        }
    }

    private static boolean isDoubleWidthChar(char c) {
        return (c & 0xffff) >= 128;
    }
}
