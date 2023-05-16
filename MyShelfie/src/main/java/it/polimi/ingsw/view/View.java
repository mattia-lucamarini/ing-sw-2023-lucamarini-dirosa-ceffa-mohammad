package it.polimi.ingsw.view;
import it.polimi.ingsw.model.Player;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.IOException;

public class View extends Application {
    public static void main(String[] args) {
        launch(args);}

    public void start(Stage stage) throws IOException {
        Parent mockup = FXMLLoader.load(getClass().getResource("/mockup.fxml"));
        stage.setTitle("MyShelfie!");
        Label label = new Label("Welcome to MyShelfie!");
        VBox root = new VBox();
        TextField username = new TextField("Type your username here");
        System.out.println(username.getText());
        Button login = new Button("Send");
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String us = username.getText();
                Player player = new Player(us);
            }
        });
        root.getChildren().addAll(label,username,login);
        root.setSpacing(20);
        root.setAlignment(Pos.CENTER);
        Scene scene1 = new Scene(mockup, 600, 400);

        stage.setScene(scene1);


        stage.show();
    }

}