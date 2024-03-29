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
/**class: View
 * This class starts the GUI written in JAVAFX from an FXML file and instantiate the Controller(the Action listener)*/
public class View extends Application{

    private ViewHandler viewhandler;
    private Stage stage1;

    public static void main(String[] args) {
        launch(args);}

    public void start(Stage stage) throws IOException, InterruptedException {
        System.out.println("1");
        stage.setTitle("MyShelfie!");
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/login.fxml")));
        loader.setController(new ViewHandler(stage,this));
        viewhandler = loader.getController();
        Parent login = loader.load();
        Scene scene1 = new Scene(login, 490, 350);
        scene1.getStylesheets().add(getClass().getResource("/test_styles.css").toExternalForm());
        stage.setScene(scene1);
        stage.show();
    }
    public ViewHandler getViewhandler(){
        return viewhandler;
    }
}

