package it.polimi.ingsw;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;

public class CommonGoalTest {
    @Test
    public void testAdjacent() {
        // . . . . .
        // . . . . .
        // . . . . .
        // P . . . .
        // P P . B .
        // P P P B .
        Shelf shelf = ShelfTest.shelf2();

        // Shouldn't pass
        Predicate<Shelf> adjacent6G2T = CommonGoal.Adjacent(6, 2);
        Assert.assertFalse(adjacent6G2T.test(shelf));

        // Should pass
        Predicate<Shelf> adjacent1G2T = CommonGoal.Adjacent(1, 2);
        Assert.assertTrue(adjacent1G2T.test(shelf));
    }
}
