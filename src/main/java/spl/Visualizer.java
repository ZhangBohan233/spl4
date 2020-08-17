package spl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import spl.ast.BlockStmt;
import spl.lexer.*;
import spl.parser.Parser;
import spl.tools.AstVisualizer;
import spl.util.ArgumentParser;

import java.io.IOException;
import java.util.Map;

public class Visualizer extends Application {

    private static BlockStmt root;
    private static Map<String, BlockStmt> importedModules;

    public static void main(String[] args) throws IOException {
        new Visualizer().load(args);

        launch();
    }

    private void load(String[] args) throws IOException {
        ArgumentParser argumentParser = new ArgumentParser(args);
        if (argumentParser.isAllValid()) {
            TokenizeResult tr =
                    new FileTokenizer(argumentParser.getMainSrcFile(), argumentParser.importLang()).tokenize();
            TextProcessResult tpr = new TextProcessor(tr, argumentParser.importLang()).process();
            root = new Parser(tpr).parse();
            importedModules = SplInterpreter.parseImportedModules(tpr.importedPaths);
        }
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
