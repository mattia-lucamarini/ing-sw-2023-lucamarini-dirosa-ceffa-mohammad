package it.polimi.ingsw.network.message;

import java.util.ArrayList;

public class PlayerOrder extends Message {
    private ArrayList<String> order;
    public PlayerOrder(ArrayList<String> order){
        super(MessageCode.PLAYER_ORDER);
        this.order = order;
    }

    public ArrayList<String> getOrder() {
        return order;
    }
}
