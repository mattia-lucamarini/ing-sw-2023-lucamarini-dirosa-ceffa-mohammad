package it.polimi.ingsw;
/**
 * Class: Cell
 * @author Angelo Di Rosa
 * This class represents the little square of the board where you put the object tile.
 *
 */
public class Cell {
    private Tile tile;

    public Cell(Tile t){
        this.tile = t;
    }
    /**
     * Method: assignValue
     * @author Angelo Di Rosa
     * @param t1
     * The method assignValue assigns one of the Enumeration Tiles values to the cell. */
    public void assignValue(Tile t1){
        this.tile = t1;
    }

    /**
     * Method: isValid()
     * @author Angelo Di Rosa
     * isValid() is used to check if the single cell of the board can be used for the game or not.
     * This because the board available for the game is not a full 9x9 matrix,
     * so with this method we check if we can put a tile on the cell.*/
    public boolean isValid(){
        if (tile == Tile.NOTVALID){
            return false;
        }
        return true;
    }
    /**
     * Method: isEmpty()
     * @author Angelo Di Rosa
     * isEmpty checks if the cell is occupied by a tile or not.
     * A tile is empty and available for the game when the "VALID" value is on it.*/
    public boolean isEmpty(){
        if (tile == Tile.VALID){
            return true;
        }
        return false;
    }
}
