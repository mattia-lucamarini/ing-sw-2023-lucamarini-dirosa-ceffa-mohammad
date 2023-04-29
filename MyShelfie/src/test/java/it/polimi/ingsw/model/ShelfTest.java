package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ShelfTest {
    public static Shelf purpleShelf1() {
        // . . . . .
        // . . . . .
        // . . . . .
        // P . . . .
        // P P . . .
        // P P P . .
        Shelf shelf = new Shelf();
        shelf.insertTiles(
                List.of(Pair.of(0, 0), Pair.of(1, 0), Pair.of(2, 0)),
                List.of(Tiles.PURPLE, Tiles.PURPLE, Tiles.PURPLE)
        );
        shelf.insertTiles(
                List.of(Pair.of(0, 1), Pair.of(1, 1)),
                List.of(Tiles.PURPLE, Tiles.PURPLE)
        );
        shelf.insertTiles(
                List.of(Pair.of(0, 2)),
                List.of(Tiles.PURPLE)
        );
        return shelf;
    }

    public static Shelf shelf2() {
        // . . . . .
        // . . . . .
        // . . . . .
        // P . . . .
        // P P . B .
        // P P P B .
        Shelf shelf = purpleShelf1();

        shelf.insertTiles(List.of(Pair.of(0, 3), Pair.of(1, 3)), List.of(Tiles.BLUE, Tiles.BLUE));

        return shelf;
    }

    @Test
    public void testFindGroups1PurpleGroup() {
        Shelf shelf = purpleShelf1();

        Assert.assertEquals(
                List.of(Pair.of(Tiles.PURPLE, 6)),
                shelf.findTileGroups()
        );
    }
    @Test
    public void testFindGroups2Groups() {
        Shelf shelf = shelf2();

        Assert.assertEquals(
                List.of(Pair.of(Tiles.PURPLE, 6), Pair.of(Tiles.BLUE, 2)),
                shelf.findTileGroups()
        );
    }
}
