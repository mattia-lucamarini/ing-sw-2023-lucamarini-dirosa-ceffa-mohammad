package it.polimi.ingsw.network.message;

/**
 * Signals an achieved common goal.
 * Contains an integer which represents the goal index.
 * @author Mattia Lucamarini
 */
public class CommonGoalReached extends Message {
    private int position;
    private String player;

    /**
     * @param pos goal index
     */
    public CommonGoalReached(int pos){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.position = pos;
    }

    /**
     * @param player username
     * @param pos goal index
     */
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
