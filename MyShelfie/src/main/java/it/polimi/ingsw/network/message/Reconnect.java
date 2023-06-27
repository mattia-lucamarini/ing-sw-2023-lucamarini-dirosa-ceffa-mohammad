package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Shelf;
import it.polimi.ingsw.model.Tiles;

import java.util.ArrayList;
import java.util.HashMap;

public class Reconnect extends Message {
    private int personalGoalIndex;
    private Pair<Integer, Integer> commonGoalIndexes;
    private int numPlayers;
    private ArrayList<String> playerOrder;
    private String nowPlaying;
    private HashMap<String, Shelf> playerShelves;

    public Reconnect(int personalGoalIndex, Pair<Integer, Integer> commonGoalIndexes, int numPlayers, ArrayList<String> playerOrder, String nowPlaying, HashMap<String, Shelf> playerShelves) {
        super(MessageCode.RECONNECT);
        this.personalGoalIndex = personalGoalIndex;
        this.commonGoalIndexes = commonGoalIndexes;
        this.numPlayers = numPlayers;
        this.playerOrder = playerOrder;
        this.nowPlaying = nowPlaying;
        this.playerShelves = playerShelves;
    }
    public int getPersonalGoalIndex() {
        return personalGoalIndex;
    }

    public Pair<Integer, Integer> getCommonGoalIndexes() {
        return commonGoalIndexes;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public ArrayList<String> getPlayerOrder() {
        return playerOrder;
    }

    public String getNowPlaying() {
        return nowPlaying;
    }

    public HashMap<String, Shelf> getPlayerShelves() {
        return playerShelves;
    }
}