package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Tiles;

import java.util.List;

/**
 * Carries the tiles and coordinates used in an 'insert' move.
 * @author Mattia Lucamarini
 */
public class Insert extends Message {
    private List<Pair<Integer, Integer>> positions;
    private List<Tiles> tiles;

    /**
     * @param positions tile coordinates
     * @param tiles tile types
     */
    public Insert(List<Pair<Integer, Integer>> positions, List<Tiles> tiles){
        super(MessageCode.INSERT);
        this.positions = positions;
        this.tiles = tiles;
    }
    public List<Pair<Integer, Integer>> getPositions() {
        return positions;
    }
    public List<Tiles> getTiles() {
        return tiles;
    }
}
