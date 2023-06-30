package it.polimi.ingsw.network.message;

import java.util.HashMap;

/**
 * Signals an achieved common goal.
 * Contains an integer which represents the goal index.
 * @author Mattia Lucamarini
 */
public class CommonGoalReached extends Message {
    private HashMap<Integer, Boolean> reached;
    private String player;

    /**
     * @param first reached goal 1
     * @param second reached goal 2
     */
    public CommonGoalReached(boolean first, boolean second){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.reached = new HashMap<>();
        reached.put(0, first);
        reached.put(1, second);
    }

    /**
     * @param player username
     * @param first reached goal 1
     * @param second reached goal 2
     */
    public CommonGoalReached(String player, boolean first, boolean second){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.reached = new HashMap<>();
        reached.put(0, first);
        reached.put(1, first);
        this.player = player;
    }

    /**
     * @param player username
     * @param reached HashMap with goal indexes as keys and bool reach conditions as values.
     */
    public CommonGoalReached(String player, HashMap<Integer, Boolean> reached){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.reached = reached;
        this.player = player;
    }
    public CommonGoalReached(String player, int index){
        super(MessageCode.COMMON_GOAL_REACHED);
        this.player = player;
        this.reached = new HashMap<>();
        reached.put(0, false);
        reached.put(1, false);
        this.reached.put(index, true);
    }

    public HashMap<Integer, Boolean> getReached() {
        return reached;
    }

    public String getPlayer() {
        return player;
    }
}
