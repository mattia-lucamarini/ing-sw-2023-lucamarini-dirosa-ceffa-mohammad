package it.polimi.ingsw.network.message;

public class CommonGoalReached extends Message {
    private int position;
    public CommonGoalReached(int pos){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.position = pos;
    }

    public int getPosition() {
        return position;
    }
}
