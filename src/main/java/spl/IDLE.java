package spl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import spl.tools.AstVisualizer;

public class IDLE extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/spl/fxml/idle.fxml"));
        Parent parent = loader.load();

        stage.setTitle("IDLE");
        stage.setScene(new Scene(parent));
        stage.show();
    }
}
