package it.polimi.ingsw.view.MessageView;
/**class: ShowPersonal
 * @author Angelo Di Rosa
 * Message class used to communicate the personal goal index so the gui can show the image*/
public class ShowPersonal extends MessageView{
    private int goalindex;
    public ShowPersonal(int goalindex){
        super(MessageCodeView.PERSONAL_GOAL);
        this.goalindex = goalindex;
    }

    public int getGoalIndex() {
        return goalindex;
    }
}
