package gui; /**
 * Created by Lennart on 6/07/16.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BPSolverGui extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("BPSolverGui.fxml"));

        Scene scene = new Scene(root);
        primaryStage.setTitle("Fantastische gui voor de geweldige solver van Lennart Van Damme");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
