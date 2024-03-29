package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Shelf;

import java.util.ArrayList;
/**class: SetComboBox
 * @author Angelo Di Rosa
 * Message class used to communicate the player's usernames and put them in the comboBox shown in the gui*/

public class SetComboBox extends MessageView{
    private ArrayList<String> usernames;

    public SetComboBox(ArrayList<String> usernames){
        super(MessageCodeView.INIT_COMBOBOX);
        this.usernames = usernames;

    }

    public ArrayList<String> getComboItems(){
        return usernames;
    }
}
