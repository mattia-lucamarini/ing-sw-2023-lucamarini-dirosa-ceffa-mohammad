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

public class View extends Application{
    private GUIClient gui;
    private ViewHandler viewhandler;
    private Stage stage1;

    public static void main(String[] args) {
        launch(args);}

    public void start(Stage stage) throws IOException, InterruptedException {
        System.out.println("1");
        stage1=stage;
        stage.setTitle("MyShelfie!");
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/login.fxml")));
        GUIClient client = new GUIClient(this);
        loader.setController(new ViewHandler(client));
        viewhandler = loader.getController();
        Parent login = loader.load();
        Scene scene1 = new Scene(login, 480, 340);
        scene1.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
        stage.setScene(scene1);
        stage.show();

    }
    public ViewHandler getViewhandler(){
        return viewhandler;
    }
    public Stage getStage(){
        return stage1;
    }
}

