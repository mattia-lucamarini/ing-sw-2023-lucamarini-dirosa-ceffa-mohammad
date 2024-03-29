package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class: Bag
 * @author Angelo Di Rosa
 * Bag is meant to represent the physical bag of the game in which all the 132 tiles are kept.
 * The class contains 6 integer, all initialized at 22 (number of tiles per type).
 */
public class Bag implements Serializable {
    int blue = 22, green = 22, purple=22, yellow=22, white=22, lightblue=22;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bag bag = (Bag) o;
        return blue == bag.blue && green == bag.green && purple == bag.purple && yellow == bag.yellow && white == bag.white && lightblue == bag.lightblue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blue, green, purple, yellow, white, lightblue);
    }

    @Override
    public String toString() {
        return "Bag{" +
                "blue=" + blue +
                ", green=" + green +
                ", purple=" + purple +
                ", yellow=" + yellow +
                ", white=" + white +
                ", lightblue=" + lightblue +
                '}';
    }

    /**Method: remainingTiles(int tilenumber)
     * @author Angelo Di Rosa
     * @param tilenumber which is the integer associated with the type of the tile
     * This method counts how many times a type of tile is "taken from the bag".
     * Depending on the value of tilenumber, remainingTiles subtracts 1 from the total amount of tiles of the same type.
     * For Example: if tilenumber is 0, it's a BLUE tile so we subtract 1 from the variable blue (total amount of blue tiles in the bag).
     * This means that a BLUE tile was taken out from the bag and is now on the board.
     * If the amount of tiles of the same type is 0, the function returns NOTVALID to signal that there are no more tiles of that type in the bag.
     * It will be refillBoard's job to generate a new random number from 0 to 5 to solve the problem and chose another tile.*/
    public Tiles remainingTiles(int tilenumber){
        if(tilenumber == 0 && blue!=0){
            --blue;
            return Tiles.BLUE;
        }
        if(tilenumber == 1 && green!=0){
            --green;
            return Tiles.GREEN;
        }
        if(tilenumber == 2 && purple!=0){
            --purple;
            return Tiles.PURPLE;
        }
        if(tilenumber == 3 && yellow!=0){
            --yellow;
            return Tiles.YELLOW;
        }
        if(tilenumber == 4 && white!=0){
            --white;
            return Tiles.WHITE;
        }
        if(tilenumber == 5 && lightblue!=0){
            --lightblue;
            return Tiles.LIGHTBLUE;
        }
        return Tiles.NOTVALID;
    }
}
