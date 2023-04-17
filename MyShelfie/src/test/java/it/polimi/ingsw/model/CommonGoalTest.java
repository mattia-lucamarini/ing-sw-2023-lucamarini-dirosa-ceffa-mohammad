package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.function.Predicate;

public class CommonGoalTest {
    @Test
    public void testAdjacent() {
        // . . . . .
        // . . . . .
        // . . . B .
        // P . . G .
        // P P . B .
        // P P P B .
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
        shelf.insertTiles(
                List.of(Pair.of(0, 2), Pair.of(1, 3)),
                List.of(Tiles.BLUE, Tiles.BLUE)
        );
        shelf.insertTiles(
                List.of(Pair.of(2, 3), Pair.of(3, 3)),
                List.of(Tiles.GREEN, Tiles.BLUE)
        );

        // Shouldn't pass
        Predicate<Shelf> adjacent6G2T = CommonGoal.Adjacent(6, 2);
        Assert.assertFalse(adjacent6G2T.test(shelf));

        // Should pass
        Predicate<Shelf> adjacent1G2T = CommonGoal.Adjacent(2, 2);
        Assert.assertTrue(adjacent1G2T.test(shelf));
    }
}
