package it.polimi.ingsw.view.MessageView;

import java.util.ArrayList;
/**class: PlayerOrderView
 * @author Angelo Di Rosa
 * Message class used to communicate the player's order*/

public class PlayerOrderView extends MessageView {
    private ArrayList<String> order;
    public PlayerOrderView(ArrayList<String> order){
        super(MessageCodeView.PLAYER_ORDER);
        this.order=order;
    }
    public ArrayList<String> getOrder(){
        return order;
    }
}
