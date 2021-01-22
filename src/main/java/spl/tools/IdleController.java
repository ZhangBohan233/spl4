package spl.tools;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import spl.Console;
import spl.SplInterpreter;
import spl.Visualizer;
import spl.interpreter.EvaluatedArguments;
import spl.interpreter.env.Environment;
import spl.interpreter.env.InstanceEnvironment;
import spl.interpreter.env.ModuleEnvironment;
import spl.interpreter.invokes.SplInvokes;
import spl.interpreter.primitives.Reference;
import spl.interpreter.primitives.SplElement;
import spl.interpreter.splObjects.*;
import spl.tools.codeArea.*;
import spl.util.Constants;
import spl.util.LineFilePos;
import spl.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

public class IdleController implements Initializable {

    private final static String arrow = ">>> ", cont = ". . . ";
    private final Set<String> builtinNames = new HashSet<>();
    @FXML
    TextArea consoleArea;
    @FXML
    CodeArea outputArea;
    @FXML
    CodeArea codeArea;
    @FXML
    TreeTableView<EnvTableItem> envTable;
    @FXML
    Button runButton, stopButton;
    @FXML
    Label memoryUseLabel;
    @FXML
    RowConstraints codeAreaRow;
    private boolean showBuiltins = true;
    private IdleIO idleIO;
    private CodeFile openingFile = new CodeFile(new File("Untitled.sp"));
    private Console console;
    private RunService runService;
    private Timer timer;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        restartConsole();
        setCodeAreaListener();
        setConsoleListener();
        setTableFactories();
        refreshTable();

        recordBuiltinNames();

        codeAreaRow.prefHeightProperty().addListener(((observable, oldValue, newValue) -> {
            codeArea.setHeight(newValue.doubleValue());
        }));
        codeArea.setCodeAnalyzer(createSplCodeAnalyzer());
        codeArea.setCodeFile(openingFile);

        outputArea.setCodePref(new CodePref.Builder()
                .autoIndent(false)
                .autoBackBrace(false)
                .build());

        timer = new Timer();
        timer.schedule(new RefreshMemoryTask(), 0, 500);
    }

    private CodeAnalyzer createSplCodeAnalyzer() {
        CodeAnalyzer codeAnalyzer = new SplCodeAnalyzer(codeArea, codeArea.getCodeFont());
        Set<String> builtinNames = new HashSet<>(console.getGlobalEnvironment().keyAttributes().keySet());

        Reference langRef = (Reference) console.getGlobalEnvironment().get("lang", LineFilePos.LFP_CONSOLE);
        SplModule langModule = console.getGlobalEnvironment().getMemory().get(langRef);
        builtinNames.addAll(langModule.getEnv().keyAttributes().keySet());

        codeAnalyzer.setBuiltinNames(builtinNames);
        return codeAnalyzer;
    }

    private void recordBuiltinNames() {
        builtinNames.addAll(console.getGlobalEnvironment().keyAttributes().keySet());
    }

    public void close() {
        codeArea.close();
        timer.cancel();
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
    void clearOutputAction() {
        outputArea.setText("");
    }

    @FXML
    void restartConsoleAction() {
        restartConsole();
    }

    @FXML
    void runAction() throws IOException {
        saveFileAction();
        SplInterpreter interpreter = new SplInterpreter();
        interpreter.setGlobalEnvironment(console.getGlobalEnvironment());
        SplInterpreter.setIn(console.getIn());
        SplInterpreter.setOut(console.getOut());
        SplInterpreter.setErr(console.getErr());
        try {
            interpreter.run(new String[]{openingFile.getFile().getAbsolutePath()});
        } catch (Exception e) {
            e.printStackTrace(console.getErr());
        }
    }

    private void runCode() {
        if (runService != null)
            if (runService.isRunning()) runService.cancel();

        setRunningUi();

        runService = new RunService();
        runService.setOnCancelled(e -> {
            console.interrupt();
            setNotRunningUi();
            refreshTable();
        });
        runService.setOnSucceeded(e -> {
            setNotRunningUi();
            refreshTable();
        });
        runService.setOnFailed(e -> {
            setNotRunningUi();
            refreshTable();
        });

        runService.start();
    }

    @FXML
    void stopAction() {
        runService.cancel();
    }

    @FXML
    void showBuiltinsChange(ActionEvent ae) {
        CheckBox checkBox = (CheckBox) ae.getSource();
        showBuiltins = checkBox.isSelected();
        refreshTable();
    }

    @FXML
    void openFileAction() throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(
                new FileChooser.ExtensionFilter("SPL source file", "*.sp"));
        chooser.setSelectedExtensionFilter(
                new FileChooser.ExtensionFilter("Compiled spl cache", "*.spc"));

        File defaultSpDir = new File(System.getProperty("user.dir") + File.separator + "sp");
        if (defaultSpDir.exists()) {
            chooser.setInitialDirectory(defaultSpDir);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            String text = Utilities.readFile(f);
            openingFile = new CodeFile(f);
            codeArea.setCodeFile(openingFile);
            codeArea.setText(text);
        }
    }

    @FXML
    void saveFileAction() throws IOException {
        String text = codeArea.getText();
        openingFile.save(text);
    }

    @FXML
    void viewAstAction() throws IOException {
        saveFileAction();
        if (!Visualizer.run(new String[]{openingFile.getFile().getAbsolutePath()}, false)) {
            idleIO.err.println("Cannot start ast visualizer");
        }
    }

    private void setRunningUi() {
        Platform.runLater(() -> {
            stopButton.setDisable(false);
            runButton.setDisable(true);
        });
    }

    private void setNotRunningUi() {
        Platform.runLater(() -> {
            stopButton.setDisable(true);
            runButton.setDisable(false);
        });
    }

    @SuppressWarnings("unchecked")
    private void setTableFactories() {
        TreeTableColumn<EnvTableItem, String> varNameCol =
                (TreeTableColumn<EnvTableItem, String>) envTable.getColumns().get(0);
        TreeTableColumn<EnvTableItem, String> valueCol =
                (TreeTableColumn<EnvTableItem, String>) envTable.getColumns().get(1);
        TreeTableColumn<EnvTableItem, String> typeCol =
                (TreeTableColumn<EnvTableItem, String>) envTable.getColumns().get(2);

        varNameCol.setCellValueFactory(p ->
                new ReadOnlyStringWrapper(p.getValue().getValue().getVarName()));
        valueCol.setCellValueFactory(p ->
                new ReadOnlyStringWrapper(p.getValue().getValue().getValueString()));
        typeCol.setCellValueFactory(p ->
                new ReadOnlyStringWrapper(p.getValue().getValue().getType()));

        valueCol.setCellFactory(col ->
                new TreeTableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item);
                            hoverProperty().addListener(((observableValue, aBoolean, t1) -> {
                                if (t1) {
                                    envTable.setTooltip(new Tooltip(item));
                                } else {
                                    envTable.setTooltip(null);
                                }
                            }));
                        }
                    }
                });
    }

    private void refreshTable() {
        TreeItem<EnvTableItem> root = new TreeItem<>(new EnvTableItem("Global", null, null));
        for (Map.Entry<String, SplElement> entry : console.getGlobalEnvironment().keyAttributes().entrySet()) {
            if (showBuiltins || !builtinNames.contains(entry.getKey())) {
                TreeItem<EnvTableItem> eti =
                        createTreeItem(entry.getKey(), entry.getValue(), console.getGlobalEnvironment());
                root.getChildren().add(eti);
            }
        }

        envTable.setRoot(root);
        root.setExpanded(true);
    }

    private TreeItem<EnvTableItem> createTreeItem(String varName, SplElement se, Environment env) {
        if (SplElement.isPrimitive(se)) {
            return new TreeItem<>(new EnvTableItem(varName, se.toString(), SplElement.typeToString(se.type())));
        } else {
            Reference ref = (Reference) se;
            if (ref.getPtr() == 0) {
                return new TreeItem<>(new EnvTableItem(varName, "null", "null"));
            }
            try {
                SplObject obj = env.getMemory().get(ref);
                TreeItem<EnvTableItem> ti;
                if (obj instanceof Instance) {
                    Instance ins = (Instance) obj;
                    InstanceEnvironment insEnv = ins.getEnv();
                    ti = new TreeItem<>(new EnvTableItem(
                            varName,
                            SplInvokes.pointerToString(ref, env, LineFilePos.LFP_CONSOLE),
                            Utilities.classRefToRepr((Reference) ((SplMethod) env.getMemory()
                                            .get((Reference)
                                                    insEnv.get(Constants.GET_CLASS, LineFilePos.LFP_CONSOLE)))
                                            .call(
                                                    EvaluatedArguments.of(ref),
                                                    console.getGlobalEnvironment(),
                                                    LineFilePos.LFP_CONSOLE),
                                    console.getGlobalEnvironment())));
                    if (varName.equals(Constants.INSTANCE_NAME)) {
                        return ti;
                    }
                    for (Map.Entry<String, SplElement> entry : insEnv.keyAttributes().entrySet()) {
                        TreeItem<EnvTableItem> eti =
                                createTreeItem(entry.getKey(), entry.getValue(), insEnv);
                        ti.getChildren().add(eti);
                    }
                } else if (obj instanceof SplModule) {
                    ModuleEnvironment modEnv = ((SplModule) obj).getEnv();
                    ti = new TreeItem<>(new EnvTableItem(
                            varName,
                            ref.toString(),
                            "Module"
                    ));
                    for (Map.Entry<String, SplElement> entry : modEnv.keyAttributes().entrySet()) {
                        TreeItem<EnvTableItem> eti =
                                createTreeItem(entry.getKey(), entry.getValue(), modEnv);
                        ti.getChildren().add(eti);
                    }
                } else if (obj instanceof SplMethod) {
                    return new TreeItem<>(new EnvTableItem(
                            varName,
                            ref.toString(),
                            "Method of " + Utilities.classRefToRepr(
                                    ((SplMethod) obj).getClassPtr(),
                                    console.getGlobalEnvironment())));
                } else if (obj instanceof Function) {
                    return new TreeItem<>(new EnvTableItem(varName, ref.toString(), "Function"));
                } else if (obj instanceof LambdaExpression) {
                    return new TreeItem<>(new EnvTableItem(varName, ref.toString(), "Lambda Function"));
                } else if (obj instanceof NativeFunction) {
                    return new TreeItem<>(new EnvTableItem(varName, ref.toString(), "Native Function"));
                } else if (obj instanceof SplArray) {
                    return new TreeItem<>(new EnvTableItem(varName, SplInvokes.pointerToString(
                            ref, env, LineFilePos.LFP_CONSOLE
                    ), obj.toString()));
                } else if (obj instanceof SplClass) {
                    return new TreeItem<>(new EnvTableItem(varName, ref.toString(), "Class"));
                } else if (obj instanceof NativeObject) {
                    return new TreeItem<>(new EnvTableItem(varName, ref.toString(), "Native Object"));
                } else {
                    return new TreeItem<>(new EnvTableItem(varName, ref.toString(), obj.getClass().getTypeName()));
                }
                return ti;
            } catch (Exception e) {
                return new TreeItem<>(new EnvTableItem(varName, ref.toString(), ""));
            }
        }
    }

    private void setCodeAreaListener() {
    }

    private void setConsoleListener() {
        consoleArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String text = consoleArea.getText();
                String processed = getLastLine(text);
                if (console.addLine(processed)) {
                    idleIO.showInputLine(text);
                    runCode();
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
            } else if (keyEvent.getCode() == KeyCode.DOWN) {
                String lastInput = idleIO.getDownLine();
                if (lastInput != null) {
                    consoleArea.setText(lastInput);
                    consoleArea.positionCaret(lastInput.length());
                }
            }
        });
        consoleArea.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.length() < 4) {
                consoleArea.setText(oldValue);
            }
        }));
    }

    private static class IdleInputStream extends InputStream {

        private final TextArea textArea;

        private IdleInputStream(TextArea area) {
            this.textArea = area;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }
    }

    private static class IdleOutputStream extends PrintStream {

        private final CodeArea textArea;
        private final Paint textColor;

        public IdleOutputStream(CodeArea area) {
            this(area, Paint.valueOf("black"));
        }

        public IdleOutputStream(CodeArea area, Paint textColor) {
            super(nullOutputStream());
            this.textColor = textColor;
            this.textArea = area;
            this.textArea.setText("");
        }

        @Override
        public void print(String s) {
            if (s == null) return;
            textArea.setCodePaint(textColor);
            for (char c : s.toCharArray()) {
                if (c == '\n') textArea.getTextEditor().newLine();
                else textArea.getTextEditor().typeText(c);
            }
            textArea.scrollToBottom();
        }

        @Override
        public void println(String s) {
            print(s);
            textArea.getTextEditor().newLine();
        }
    }

    static class EnvTableItem {

        private final String name;
        private final String type;
        private final String value;

        EnvTableItem(String name, String value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        String getVarName() {
            return name;
        }

        String getType() {
            return type;
        }

        String getValueString() {
            return value;
        }
    }

    private class RunService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    console.runCode();
                    return null;
                }
            };
        }
    }

    private class IdleIO {
        private final List<String> inputLines = new ArrayList<>();
        private final IdleOutputStream out = new IdleOutputStream(outputArea);
        private final IdleOutputStream err = new IdleOutputStream(outputArea, Paint.valueOf("red"));
        private final IdleInputStream in = new IdleInputStream(consoleArea);
        private int upCount = 0;

        private void showInputLine(String input) {
            inputLines.add(input);
            out.print(input);
            upCount = 0;
            out.textArea.scrollToBottom();
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

    private class RefreshMemoryTask extends TimerTask {
        @Override
        public void run() {
            Platform.runLater(() -> memoryUseLabel.setText(
                    Utilities.sizeToReadable(console.getGlobalEnvironment().getMemory().getHeapUsed()) +
                            " / " +
                            Utilities.sizeToReadable(console.getGlobalEnvironment().getMemory().getHeapSize())
            ));
        }
    }
}
