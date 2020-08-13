package spl;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Visualizer extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/spl/fxml/visualizer.fxml"));
        Parent parent = loader.load();

        primaryStage.setTitle("Abstract Syntax Tree spl.Visualizer");
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();
    }
}
