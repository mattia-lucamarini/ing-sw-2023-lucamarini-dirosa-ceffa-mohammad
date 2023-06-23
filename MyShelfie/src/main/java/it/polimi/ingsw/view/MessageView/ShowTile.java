package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Tiles;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class ShowTile extends MessageView{
    private boolean shelforgrid; // zero fors shelf, one for grid
    private List<Tiles> tiles;
    private ArrayList<Pair<Integer, Integer>> positions;
    public ShowTile(boolean shelforgrid, List<Tiles> tiles, ArrayList<Pair<Integer, Integer>> positions){
        super(MessageCodeView.SHOW_TILE);
        this.shelforgrid=shelforgrid;
        this.tiles = tiles;
        this.positions=positions;
    }

    public List<Tiles> getTilesToShow(){
        return tiles;
    }
    public ArrayList<Pair<Integer, Integer>> getPositionsToShow(){
        return positions;
    }
    public boolean getShelfOrGrid(){
        return shelforgrid;
    }
}
