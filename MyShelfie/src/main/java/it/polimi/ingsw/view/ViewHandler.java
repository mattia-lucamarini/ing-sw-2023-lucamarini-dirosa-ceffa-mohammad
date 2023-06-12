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
    private final GUIClient client;
    private int flag=-1, nump;
    private String username;
    private String conn;
    private Stage stage;
    private GUIInterface gui;
    private boolean answer = false;
    @FXML
    TextField text;
    @FXML
    RadioButton socket, rmi, two, three, four;
    @FXML
    Button button;
    @FXML
    ToggleGroup connection, numplayers;
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
    ImageView stack1, stack2, iw1;
    public ViewHandler(GUIClient client){
        this.client = client;
    }

    public void login(ActionEvent e){
        System.out.println("3");
        username = text.getText();
        if(connection.getSelectedToggle()==socket){
            conn = "socket";
        }
        else {
            conn = "rmi";
        }
        System.out.println("4");
        client.data();
        //System.out.println("Username "+ username + "\nConnection selected: "+ conn);
        //send these info to the user interface when done.
        //if it is the first user to log in, it shows the number of players scene.
        //num of players scene.
        //if it is not the first user to log in, shows the game board etc..
        //Parent game = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/mockup.fxml")));
        /*Pane gamelayout = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/game.fxml")));
        boardgrid = (GridPane) gamelayout.getChildren().get(0);
        shelfgrid = (GridPane) gamelayout.getChildren().get(1);
        iw1 = (ImageView) boardgrid.getChildren().get(0);
        iw1.setImage(new Image(getClass().getResource("/assets/item tiles/Cornici1.1.png").toExternalForm()));
        Scene gameplay = new Scene(gamelayout, 800, 601);
        stage = (Stage)(((Node)e.getSource()).getScene().getWindow());
        stage.setScene(gameplay);
        stage.show();*/
    }
    public void numberOfPlayersController(ActionEvent e){
            if(numplayers.getSelectedToggle()==two){
                nump=2;
            }
            else if(numplayers.getSelectedToggle()==three){
                nump=3;
            }
            else if(numplayers.getSelectedToggle()==four){
                nump=4;
            }
    }
    public void boardcellselected(ActionEvent e){

    }
    public void shelfcellselected(ActionEvent e){

    }

    public String getUsername(){
        return username;
    }
    public int getNump(){
        return nump;
    }
    public Boolean getAnswer(){
        return answer;
    }
}
