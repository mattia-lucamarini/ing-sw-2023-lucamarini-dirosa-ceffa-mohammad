package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Before;
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

    @Test
    public void testCheckGoalColumns() {
        var shelf = ShelfTest.shelf2();

        var oneRow2Colors = new CommonGoal(
                CommonGoalPredicate.Columns(2, 3, 1),
                List.of(1, 2, 3)
        );

        // TEST: Bottom column is not complete -> 0 points.
        Assert.assertEquals(0, oneRow2Colors.checkGoal(shelf));

        // Complete the first column.
        shelf.insertTiles(
                List.of(Pair.of(3, 0), Pair.of(4, 0), Pair.of(5, 0)),
                List.of(Tiles.BLUE, Tiles.BLUE, Tiles.BLUE)
        );

        // TEST: Now first column is complete -> 3 points; 2 points.
        Assert.assertEquals(3, oneRow2Colors.checkGoal(shelf));
        Assert.assertEquals(2, oneRow2Colors.checkGoal(shelf));

        // Add another color.
        shelf.insertTiles(List.of(Pair.of(1, 0)), List.of(Tiles.GREEN), true);

        // TEST: Now first column has 3 colors (max) -> 1 points; 0.
        Assert.assertEquals(1, oneRow2Colors.checkGoal(shelf));
        Assert.assertEquals(0, oneRow2Colors.checkGoal(shelf));
        Assert.assertEquals(0, oneRow2Colors.checkGoal(shelf));
    }

    @Test
    public void testCheckGoalScatter() {
        var shelf = ShelfTest.shelf2();
        var goal = new CommonGoal(
                CommonGoalPredicate.Scatter(7),
                List.of(1, 2, 3)
        );

        // TEST: 6 purples -> 0 points.
        Assert.assertEquals(0, goal.checkGoal(shelf));

        // Add 2 purples in different group.
        shelf.insertTiles(
                List.of(Pair.of(0, 4), Pair.of(1, 4)),
                List.of(Tiles.PURPLE, Tiles.PURPLE)
        );

        // TEST: Now 8 purples total -> 3 points; 2 points.
        Assert.assertEquals(3, goal.checkGoal(shelf));
        Assert.assertEquals(2, goal.checkGoal(shelf));
        Assert.assertEquals(1, goal.checkGoal(shelf));
        Assert.assertEquals(0, goal.checkGoal(shelf));
    }

    @Test
    public void testCheckGoalShapePointedSquare() {
        var shelf = ShelfTest.shelf2();
        shelf.insertTiles(List.of(Pair.of(1, 2)), List.of(Tiles.GREEN));
        var shape = List.of(
                Pair.of(0, 0), Pair.of(0, 1), Pair.of(1, 0),
                Pair.of(1, 1), Pair.of(0, 2), Pair.of(1, 2)
        );
        var goal = new CommonGoal(
                CommonGoalPredicate.Shape(shape, 3, 2, 1),
                List.of(1, 2, 3)
        );

        // TEST: Shape not there -> 0 points.
        Assert.assertEquals(0, goal.checkGoal(shelf));

        // Complete the shape.
        shelf.insertTiles(List.of(Pair.of(1, 2)), List.of(Tiles.PURPLE), true);

        // TEST: Now shape is complete -> 3 points; 2 points.
        Assert.assertEquals(3, goal.checkGoal(shelf));
        Assert.assertEquals(2, goal.checkGoal(shelf));
        Assert.assertEquals(1, goal.checkGoal(shelf));
        Assert.assertEquals(0, goal.checkGoal(shelf));
        Assert.assertEquals(0, goal.checkGoal(shelf));
    }

    @Test
    public void testCheckGoalStairs() {
        var shelf = ShelfTest.stairsShelf();
        shelf.removeTiles(4, 1);
        var goal = new CommonGoal(
                CommonGoalPredicate.Stairs(),
                List.of(1, 2, 3)
        );

        // TEST: One missing -> 0 points.
        Assert.assertEquals(0, goal.checkGoal(shelf));

        // Complete the stairs by adding yellow back in.
        shelf.insertTiles(List.of(Pair.of(0, 4)), List.of(Tiles.YELLOW));

        // TEST: Now stairs are complete -> 3 points; 2 points.
        Assert.assertEquals(3, goal.checkGoal(shelf));
        Assert.assertEquals(2, goal.checkGoal(shelf));

        // Add another tile.
        shelf.insertTiles(List.of(Pair.of(1, 4)), List.of(Tiles.YELLOW));

        // TEST: Stairs are still there -> 1 points; 0.
        Assert.assertEquals(1, goal.checkGoal(shelf));
        Assert.assertEquals(0, goal.checkGoal(shelf));
        Assert.assertEquals(0, goal.checkGoal(shelf));

        // Invert stairs.
        shelf.reflect();

        // TEST: Stairs are still there -> 3 points; 2; 0.
        goal.rechargePoints(List.of(2, 3));
        Assert.assertEquals(3, goal.checkGoal(shelf));
        Assert.assertEquals(2, goal.checkGoal(shelf));
        Assert.assertEquals(0, goal.checkGoal(shelf));
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
