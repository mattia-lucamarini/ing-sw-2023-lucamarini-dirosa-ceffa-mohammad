package it.polimi.ingsw.view;

import it.polimi.ingsw.model.Player;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


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
    public void login(ActionEvent e) throws IOException {
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
        Parent mockup = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/mockup.fxml")));
        Scene gameplay = new Scene(mockup, 600, 390);
        stage = (Stage)(((Node)e.getSource()).getScene().getWindow());
        stage.setScene(gameplay);
        stage.show();
    }

}
