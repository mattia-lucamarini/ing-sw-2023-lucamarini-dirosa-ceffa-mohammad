package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tiles;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;

import javax.management.Notification;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;


public class ViewHandler {
    private int flag=-1;
    private String username;
    private String conn;
    private Stage stage;
    @FXML
    TextField text;
    @FXML
    RadioButton socket;
    @FXML
    RadioButton rmi;
    @FXML
    Button button;
    @FXML
    ToggleGroup connection;
    @FXML
    Pane gamelayout;
    @FXML
    GridPane boardgrid;
    @FXML
    GridPane shelfgrid;
    @FXML
    ImageView commongoal1;
    @FXML
    ImageView commongoal2;
    @FXML
    ImageView stack1;
    @FXML
    ImageView stack2;

    public void login(ActionEvent e) throws IOException {
        Label errorlabel = new Label("Please choose a valid username.");
        Popup popup = new Popup();
        popup.getContent().add(errorlabel);
        username = text.getText();
        if(connection.getSelectedToggle()==socket){
            conn = "socket";
        }
        else{
            conn = "rmi";
        }
        //System.out.println("Username "+ username + "\nConnection selected: "+ conn);
        //send these info to the user interface when done.
        //if it is the first user to log in, it shows the number of players scene.
        //num of players scene.
        //if it is not the first user to log in, shows the game board etc..
        //Parent game = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/mockup.fxml")));
        Pane gamelayout = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/game.fxml")));
        boardgrid = (GridPane) gamelayout.getChildren().get(0);
        shelfgrid = (GridPane) gamelayout.getChildren().get(1);
        //boardgrid.add(new ImageView("Cornici1.1.png") ,0,0);
        Scene gameplay = new Scene(gamelayout, 800, 601);
        stage = (Stage)(((Node)e.getSource()).getScene().getWindow());
        stage.setScene(gameplay);
        stage.show();
    }
    public void boardcellselected(ActionEvent e){

    }
    public void shelfcellselected(ActionEvent e){

    }

}
