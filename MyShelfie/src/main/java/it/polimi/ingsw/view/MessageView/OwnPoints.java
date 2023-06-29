package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.network.message.Message;
/**class: OwnPoints
 * @author Angelo Di Rosa
 * Message class used to update the common goal points the player has gained */
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
