package it.polimi.ingsw.model;

import java.io.Serializable;

/**
 * Class: Cell
 * @author Angelo Di Rosa
 * This class represents the little square of the board where you put the object tile.
 *
 */
public class Cell implements Serializable {
    private Tiles tile;

    public Cell(Tiles t){
        this.tile = t;
    }
    /**
     * Method: assignValue
     * @author Angelo Di Rosa
     * @param t1
     * The method assignValue assigns one of the Enumeration Tiles values to the cell. */
    public void assignValue(Tiles t1){
        this.tile = t1;
    }

    /**
     * Method: isNotValid()
     * @author Angelo Di Rosa
     * isValid() is used to check if the single cell of the board can be used for the game or not.
     * This because the board available for the game is not a full 9x9 matrix,
     * so with this method we check if we can put a tile on the cell.*/
    public boolean isNotValid(){
        if (tile == Tiles.NOTVALID){
            return true;
        }
        return false;
    }
    /**
     * Method: isEmpty()
     * @author Angelo Di Rosa
     * isEmpty checks if the cell is occupied by a tile or not.
     * A tile is empty and available for the game when the "VALID" value is on it.*/
    public boolean isEmpty(){
        if (tile == Tiles.VALID){
            return true;
        }
        return false;
    }

    public Tiles getTile(){
        return tile;
    }
}
