package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;

public class SetCommonGoals extends Message {
    private Pair<Integer, Integer> goalIndexes;
    private int numPlayers;
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
