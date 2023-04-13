package it.polimi.ingsw;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ShelfTest {
    public static Shelf purpleShelf1() {
        Shelf shelf = new Shelf();
        shelf.insertTiles(
                List.of(Pair.of(0, 0), Pair.of(1, 0), Pair.of(2, 0)),
                List.of(Tile.PURPLE, Tile.PURPLE, Tile.PURPLE)
        );
        shelf.insertTiles(
                List.of(Pair.of(0, 1), Pair.of(1, 1)),
                List.of(Tile.PURPLE, Tile.PURPLE)
        );
        shelf.insertTiles(
                List.of(Pair.of(0, 2)),
                List.of(Tile.PURPLE)
        );
        return shelf;
    }

    public static Shelf shelf2() {
        Shelf shelf = purpleShelf1();

        shelf.insertTiles(List.of(Pair.of(0, 3), Pair.of(1, 3)), List.of(Tile.BLUE, Tile.BLUE));

        return shelf;
    }

    @Test
    public void testFindGroups1PurpleGroup() {
        Shelf shelf = purpleShelf1();

        Assert.assertEquals(
                List.of(Pair.of(Tile.PURPLE, 6)),
                shelf.findTileGroups()
        );
    }
    @Test
    public void testFindGroups2Groups() {
        Shelf shelf = shelf2();

        Assert.assertEquals(
                List.of(Pair.of(Tile.PURPLE, 6), Pair.of(Tile.BLUE, 2)),
                shelf.findTileGroups()
        );
    }
}
