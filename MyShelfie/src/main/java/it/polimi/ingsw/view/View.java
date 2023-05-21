package it.polimi.ingsw.view;
import it.polimi.ingsw.model.Player;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Objects;

public class View extends Application {
    public static void main(String[] args) {
        launch(args);}

    public void start(Stage stage) throws IOException {
        Parent login = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/login.fxml")));
        stage.setTitle("MyShelfie!");
        Scene scene1 = new Scene(login, 480, 340);
        scene1.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
        stage.setScene(scene1);
        stage.show();
    }
}
