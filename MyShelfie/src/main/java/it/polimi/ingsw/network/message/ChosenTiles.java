package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;

import java.util.ArrayList;

public class ChosenTiles extends Message {
    private ArrayList<Pair<Integer, Integer>> playerMove;
    public ChosenTiles(ArrayList<Pair<Integer, Integer>> move){
        super(MessageCode.CHOSEN_TILES);
        this.playerMove = move;
    }
    public ArrayList<Pair<Integer, Integer>> getPlayerMove() {
        return playerMove;
    }
}
