package it.polimi.ingsw.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class NumOfPlayersHandler {
    private int nump;
    @FXML
    ToggleGroup numplayers;
    @FXML
    RadioButton two, three, four;



    public int getNumPlayers(){
        return nump;
    }
}
