package it.polimi.ingsw.model;
/**
 * Enumeration : Tiles
 * @author Angelo Di Rosa
 * This enumeration is the abstraction of the different types of Object tiles from the game. The Object tiles are differentiated by colors.
 * The values NOTVALID and VALID are used to indicate if a single cell can be used for the game*/

public enum Tiles {
        BLUE,
        GREEN,
        PURPLE,
        YELLOW,
        WHITE,
        LIGHTBLUE,
        NOTVALID,
        VALID;

        public boolean isEmpty() {
                return this == Tiles.VALID || this == Tiles.NOTVALID;
        }
}

