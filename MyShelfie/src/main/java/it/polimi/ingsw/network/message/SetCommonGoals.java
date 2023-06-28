package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;

/**
 * Used the set the common goals at the start of the game.
 * @author Mattia Lucamarini
 */
public class SetCommonGoals extends Message {
    private Pair<Integer, Integer> goalIndexes;
    private int numPlayers;

    /**
     * @param goals a pair of goal indexes
     * @param players players number used to compute the goals
     */
    public SetCommonGoals(Pair<Integer, Integer> goals, int players) {
        super(MessageCode.SET_COMMON_GOALS);
        this.goalIndexes = goals;
        this.numPlayers = players;
    }
    public Pair<Integer, Integer> getGoalsIndexes() {
        return goalIndexes;
    }
    public int getNumPlayers() {
        return numPlayers;
    }
}
