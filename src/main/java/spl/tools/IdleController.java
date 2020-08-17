package spl.tools;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import spl.Console;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class IdleController implements Initializable {

    private final static String arrow = ">>> ", cont = ". . . ";

    @FXML
    TextArea codeArea, consoleArea, outputArea;

    private File openingFile;
    private Console console;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        restartConsole();
        setConsoleListener();
    }

    private void setConsoleListener() {
        consoleArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String text = consoleArea.getText();
                String processed = getLastLine(text);
                if (console.addCode(processed)) {
                    outputArea.setText(outputArea.getText() + text);
                    console.runCode();
                    consoleArea.setText(arrow);
                    consoleArea.positionCaret(arrow.length());
                } else {
                    String nexText = text + cont;
                    consoleArea.setText(nexText);
                    consoleArea.positionCaret(nexText.length());
                }
            }
        });
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
        outputArea.setText("");
        try {
            console = new Console(new IdleOutputStream(outputArea));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clearConsoleAction() {

    }

    @FXML
    void runAction() {

    }

    @FXML
    void stopAction() {

    }

    @FXML
    void openFileAction() {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("SPL source file", "*.sp"));

        File defaultSpDir = new File(System.getProperty("user.dir") + File.separator + "sp");
        if (defaultSpDir.exists()) {
            chooser.setInitialDirectory(defaultSpDir);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            System.out.println(f);
        }
    }

    @FXML
    void saveFileAction() {

    }

    private static class IdleOutputStream extends PrintStream {

        private final TextArea textArea;

        public IdleOutputStream(TextArea area) {
            super(nullOutputStream());
            this.textArea = area;
        }

        @Override
        public void print(String s) {
            textArea.setText(textArea.getText() + s);
        }

        @Override
        public void println(String s) {
            print(s);
            print("\n");
        }
    }
}
