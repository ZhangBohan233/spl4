package spl.tools;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import spl.Console;
import spl.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class IdleController implements Initializable {

    private final static String arrow = ">>> ", cont = ". . . ";

    @FXML
    TextArea codeArea, consoleArea, outputArea;

    private IdleIO idleIO;

    private File openingFile;
    private Console console;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        restartConsole();
        setCodeAreaListener();
        setConsoleListener();
    }

    private void setCodeAreaListener() {
        codeArea.caretPositionProperty().addListener((observableValue, number, t1) -> {
            int index = t1.intValue() - 1;
            String code = codeArea.getText();
            if (index < 0 || index >= code.length()) return;
            if (code.charAt(index) == '}') {
                System.out.println(12321);
            }
        });
    }

    private void setConsoleListener() {
        consoleArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String text = consoleArea.getText();
                String processed = getLastLine(text);
                if (console.addCode(processed)) {
                    idleIO.showInputLine(text);
                    console.runCode();
                    consoleArea.setText(arrow);
                    consoleArea.positionCaret(arrow.length());
                } else {
                    String nexText = text + cont;
                    consoleArea.setText(nexText);
                    consoleArea.positionCaret(nexText.length());
                }
            } else if (keyEvent.getCode() == KeyCode.UP) {
                String lastInput = idleIO.getUpLine();
                if (lastInput != null) {
                    consoleArea.setText(lastInput);
                    consoleArea.positionCaret(lastInput.length());
                }
            }else if (keyEvent.getCode() == KeyCode.DOWN) {
                String lastInput = idleIO.getDownLine();
                if (lastInput != null) {
                    consoleArea.setText(lastInput);
                    consoleArea.positionCaret(lastInput.length());
                }
            }
        });
    }

    private static void analyze(String sourceCode) {

    }

    private static String getLastLine(String consoleInput) {
        String[] lines = consoleInput.split("\n");
        String s = lines[lines.length - 1];
        String res;
        if (s.startsWith(arrow)) {
            res = s.substring(arrow.length());
        } else if (s.startsWith(cont)) {
            res = s.substring(cont.length());
        } else {
            res = s;
        }
        return res;
    }

    private void restartConsole() {
        idleIO = new IdleIO();
        try {
            console = new Console(idleIO.in, idleIO.out, idleIO.err);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clearConsoleAction() {
        restartConsole();
    }

    @FXML
    void runAction() {
        String srcCode = codeArea.getText();
        if (console.addCode(srcCode)) {
            console.runCode();
        } else {
            idleIO.err.println("Cannot run");
        }

    }

    @FXML
    void stopAction() {

    }

    @FXML
    void openFileAction() throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("SPL source file", "*.sp"));

        File defaultSpDir = new File(System.getProperty("user.dir") + File.separator + "sp");
        if (defaultSpDir.exists()) {
            chooser.setInitialDirectory(defaultSpDir);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            String text = Utilities.readFile(f);
            codeArea.setText(text);
        }
    }

    @FXML
    void saveFileAction() {

    }

    private class IdleIO {
        private IdleOutputStream out = new IdleOutputStream(outputArea);
        private IdleOutputStream err = new IdleOutputStream(outputArea);
        private IdleInputStream in = new IdleInputStream(consoleArea);

        private final List<String> inputLines = new ArrayList<>();
        private int upCount = 0;

        private void showInputLine(String input) {
            inputLines.add(input);
            out.textArea.setText(out.textArea.getText() + input);
            upCount = 0;
            out.textArea.setScrollTop(out.textArea.getHeight());
        }

        private String getUpLine() {
            if (upCount < inputLines.size()) upCount += 1;
            if (inputLines.isEmpty()) return null;
            else return inputLines.get(inputLines.size() - upCount);
        }

        private String getDownLine() {
            if (upCount > 0) upCount -= 1;
            if (upCount == 0 || inputLines.isEmpty()) return null;
            else return inputLines.get(inputLines.size() - upCount);
        }
    }

    private static class IdleInputStream extends InputStream {

        private final TextArea textArea;

        @Override
        public int read() throws IOException {
            return 0;
        }

        private IdleInputStream(TextArea area) {
            this.textArea = area;
        }
    }

    private static class IdleOutputStream extends PrintStream {

        private final TextArea textArea;

        public IdleOutputStream(TextArea area) {
            super(nullOutputStream());
            this.textArea = area;
            this.textArea.setText("");
        }

        @Override
        public void print(String s) {
            textArea.setText(textArea.getText() + s);
            textArea.setScrollTop(textArea.getHeight());
        }

        @Override
        public void println(String s) {
            print(s);
            print("\n");
        }
    }
}
