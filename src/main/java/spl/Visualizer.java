package spl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import spl.ast.BlockStmt;
import spl.lexer.FileTokenizer;
import spl.lexer.TextProcessResult;
import spl.lexer.TextProcessor;
import spl.lexer.TokenizeResult;
import spl.parser.ParseResult;
import spl.parser.Parser;
import spl.tools.AstVisualizer;
import spl.util.ArgumentParser;

import java.io.IOException;
import java.util.Map;

public class Visualizer extends Application {

    private static BlockStmt root;
    private static Map<String, ParseResult> importedModules;

    public static void main(String[] args) throws IOException {
        run(args, true);
    }

    public static boolean run(String[] args, boolean firstRun) throws IOException {
        Visualizer visualizer = new Visualizer();
        if (visualizer.load(args)) {
            if (firstRun) {
                launch();
            } else {
                Stage stage = new Stage();
                visualizer.start(stage);
            }
            return true;
        }
        return false;
    }

    private boolean load(String[] args) throws IOException {
        ArgumentParser argumentParser = new ArgumentParser(args);
        if (argumentParser.isAllValid()) {
            TokenizeResult tr =
                    new FileTokenizer(argumentParser.getMainSrcFile(), argumentParser.importLang()).tokenize();
            TextProcessResult tpr = new TextProcessor(tr, argumentParser.importLang()).process();
            Parser psr = new Parser(tpr);
            root = psr.parse().getRoot();
            importedModules = SplInterpreter.parseImportedModules(tpr.importedPaths, psr.getStringLiterals());
            return true;
        } else return false;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/spl/fxml/visualizer.fxml"));
        Parent parent = loader.load();

        AstVisualizer visualizer = loader.getController();
        visualizer.setup(root, importedModules);

        primaryStage.setTitle("Abstract Syntax Tree Visualizer");
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();
    }
}
