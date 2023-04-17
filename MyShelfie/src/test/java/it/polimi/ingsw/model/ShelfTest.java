package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ShelfTest {
    @Test
    public void testFindGroups() {
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

        var actual = shelf.findTileGroups();
        var expected = List.of(Pair.of(Tiles.PURPLE, 6));

        Assert.assertEquals(expected, actual);
    }
}
