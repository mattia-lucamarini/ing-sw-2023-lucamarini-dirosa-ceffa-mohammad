package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.network.message.Message;

public class OwnPoints extends MessageView {
    private int points;
    private int index;
    public OwnPoints(int points, int index){
        super(MessageCodeView.UPDATE_PERSONALSTACK);
        this.points = points;
    }
    public int getPoints() {
        return points;
    }
    public int getIndex(){
        return index;
    }
}
