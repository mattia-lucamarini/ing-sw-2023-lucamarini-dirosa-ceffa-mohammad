package it.polimi.ingsw.view.MessageView;

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
