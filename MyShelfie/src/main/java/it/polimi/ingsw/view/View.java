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
        Parent mockup = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/mockup.fxml")));
        stage.setTitle("MyShelfie!");
        Label label = new Label("Welcome to MyShelfie!");
        Font font = Font.font("Arial", 14);
        label.setFont(font);
        VBox root = new VBox();
        TextField username = new TextField("Type your username here");
        username.setFont(font);
        System.out.println(username.getText());
        Label l2 = new Label("Choose which connection you would like to use;");
        l2.setFont(font);
        ToggleGroup group = new ToggleGroup();
        RadioButton socket = new RadioButton("SOCKET");
        socket.setFont(font);
        socket.setUserData("Socket");
        socket.setToggleGroup(group);
        RadioButton rmi = new RadioButton("RMI");
        rmi.setFont(font);
        rmi.setUserData("RMI");
        rmi.setToggleGroup(group);
        Button login = new Button("Send");
        login.setFont(font);
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String us = username.getText();
                Player player = new Player(us);
                if(group.getSelectedToggle()==rmi){
                    //do rmi -- to discuss
                }
                else{
                    //do socket -- to discuss
                }
            }
        });
        root.getChildren().addAll(label,username,l2,socket,rmi,login);
        root.setSpacing(20);
        root.setAlignment(Pos.CENTER);
        Scene scene1 = new Scene(mockup, 600, 400);
        scene1.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
        stage.setScene(scene1);

        stage.show();
    }

}
