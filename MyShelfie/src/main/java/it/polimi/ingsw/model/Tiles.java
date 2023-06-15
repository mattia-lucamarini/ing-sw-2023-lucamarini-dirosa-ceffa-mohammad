package it.polimi.ingsw.model;

import java.io.Serializable;

/**
 * Enumeration : Tiles
 * @author Angelo Di Rosa
 * This enumeration is the abstraction of the different types of Object tiles from the game. The Object tiles are differentiated by colors.
 * The values NOTVALID and VALID are used to indicate if a single cell can be used for the game*/

public enum Tiles implements Serializable {
        BLUE,
        GREEN,
        PURPLE,
        YELLOW,
        WHITE,
        LIGHTBLUE,
        NOTVALID,
        VALID;

        public static Tiles fromChar(char c) {
                if (c == 'G') {
                        return Tiles.GREEN;
                } else if (c == 'Y') {
                        return Tiles.YELLOW;
                } else if (c == 'P') {
                        return Tiles.PURPLE;
                } else if (c == 'W') {
                        return Tiles.WHITE;
                } else if (c == 'B') {
                        return Tiles.BLUE;
                } else if (c == 'L') {
                        return Tiles.LIGHTBLUE;
                } else if (c == 'V') {
                        return Tiles.VALID;
                } else if (c == 'I') { // NB 'I' for invalid
                        return Tiles.NOTVALID;
                } else {
                        throw new RuntimeException("Malformed tile character: " + c);
                }
        }

        public boolean isEmpty() {
                return this == Tiles.VALID || this == Tiles.NOTVALID;
        }
}

