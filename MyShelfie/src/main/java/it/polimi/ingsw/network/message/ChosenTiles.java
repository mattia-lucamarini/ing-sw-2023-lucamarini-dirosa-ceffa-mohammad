package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the tiles extracted in a 'take' move.
 * @author Mattia Lucamarini
 */
public class ChosenTiles extends Message {
    private ArrayList<Pair<Integer, Integer>> playerMove;

    /**
     * @param move tile coordinates
     */
    public ChosenTiles(List<Pair<Integer, Integer>> move){
        super(MessageCode.CHOSEN_TILES);
        this.playerMove = new ArrayList<>(move);
    }
    public ArrayList<Pair<Integer, Integer>> getPlayerMove() {
        return playerMove;
    }
}
