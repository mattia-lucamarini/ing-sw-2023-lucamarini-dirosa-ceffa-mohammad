package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.function.Predicate;

public class CommonGoalTest {
    @Test
    public void TestCheckGoalAdjacent() {
        Shelf shelf = ShelfTest.shelf2();

        // Shouldn't pass
        CommonGoal adjacent6G2T = new CommonGoal(
                CommonGoalPredicate.Adjacent(6, 2),
                List.of(1, 2, 3)
        );
        Assert.assertEquals(0, adjacent6G2T.checkGoal(shelf));

        // Should pass
        CommonGoal adjacent1G2T = new CommonGoal(
                CommonGoalPredicate.Adjacent(1, 2),
                List.of(1, 2, 3)
        );
        Assert.assertEquals(3, adjacent1G2T.checkGoal(shelf));
        Assert.assertEquals(2, adjacent1G2T.checkGoal(shelf));
        Assert.assertEquals(1, adjacent1G2T.checkGoal(shelf));
        Assert.assertEquals(0, adjacent1G2T.checkGoal(shelf));
        Assert.assertEquals(0, adjacent1G2T.checkGoal(shelf));
    }

    @Test
    public void testCheckGoalRows() {
        var shelf = ShelfTest.shelf2();

        var oneRow2Colors = new CommonGoal(
                CommonGoalPredicate.Rows(2, 3, 1),
                List.of(1, 2, 3)
        );

        // TEST: Bottom row is not complete -> 0 points.
        Assert.assertEquals(0, oneRow2Colors.checkGoal(shelf));

        // Complete the row.
        shelf.insertTiles(List.of(Pair.of(0, 4)), List.of(Tiles.PURPLE));

        // TEST: Now bottom row is complete -> 3 points; 2 points.
        Assert.assertEquals(3, oneRow2Colors.checkGoal(shelf));
        Assert.assertEquals(2, oneRow2Colors.checkGoal(shelf));

        // Add another color.
        shelf.insertTiles(List.of(Pair.of(0, 2)), List.of(Tiles.GREEN), true);

        // TEST: Now bottom row has 3 colors (max) -> 1 points; 0.
        Assert.assertEquals(1, oneRow2Colors.checkGoal(shelf));
        Assert.assertEquals(0, oneRow2Colors.checkGoal(shelf));
        Assert.assertEquals(0, oneRow2Colors.checkGoal(shelf));
    }

    // Testiamo la funzione "all"
    @Test
    public void testAllRandomDraw() {
        Shelf shelf = ShelfTest.shelf2();

        // Get all goals and shuffle them. Then check if checkGoal returns correct points.
        var allGoals = CommonGoal.all(4);
        Collections.shuffle(allGoals);
        for (var goal : allGoals) {
            Assert.assertTrue(List.of(2, 4, 6, 8, 0).contains(goal.checkGoal(shelf)));
        }
    }
}
