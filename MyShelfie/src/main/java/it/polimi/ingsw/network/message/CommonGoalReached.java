package it.polimi.ingsw.network.message;

public class CommonGoalReached extends Message {
    private int position;
    private String player;
    public CommonGoalReached(int pos){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.position = pos;
    }

    public CommonGoalReached(String player, int pos){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.position = pos;
        this.player = player;
    }

    public int getPosition() {
        return position;
    }

    public String getPlayer() {
        return player;
    }
}
