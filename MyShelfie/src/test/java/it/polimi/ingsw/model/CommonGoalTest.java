package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class CommonGoalTest {
    private int takePointsIfGoal(CommonGoal goal, Shelf shelf) {
        if (goal.checkGoal(shelf) == 1) {
            return goal.takePoints();
        }
        else return 0;
    }

    @Test
    public void TestCheckGoalAdjacent() {
        Shelf shelf = ShelfTest.shelf2();

        // Shouldn't pass
        CommonGoal adjacent6G2T = new CommonGoal(
                CommonGoalPredicate.Adjacent(6, 2),
                List.of(1, 2, 3)
        );
        Assert.assertEquals(0, takePointsIfGoal(adjacent6G2T, shelf));

        // Should pass
        CommonGoal adjacent1G2T = new CommonGoal(
                CommonGoalPredicate.Adjacent(1, 2),
                List.of(1, 2, 3)
        );
        Assert.assertEquals(3, takePointsIfGoal(adjacent1G2T, shelf));
        Assert.assertEquals(2, takePointsIfGoal(adjacent1G2T, shelf));
        Assert.assertEquals(1, takePointsIfGoal(adjacent1G2T, shelf));
        Assert.assertEquals(0, takePointsIfGoal(adjacent1G2T, shelf));
        Assert.assertEquals(0, takePointsIfGoal(adjacent1G2T, shelf));
    }

    @Test
    public void testCheckGoalRows() {
        var shelf = ShelfTest.shelf2();

        var oneRow2Colors = new CommonGoal(
                CommonGoalPredicate.Rows(2, 3, 1),
                List.of(1, 2, 3)
        );

        // TEST: Bottom row is not complete -> 0 points.
        Assert.assertEquals(0, takePointsIfGoal(oneRow2Colors, shelf));

        // Complete the row.
        shelf.insertTiles(List.of(Pair.of(0, 4)), List.of(Tiles.PURPLE));

        // TEST: Now bottom row is complete -> 3 points; 2 points.
        Assert.assertEquals(3, takePointsIfGoal(oneRow2Colors, shelf));
        Assert.assertEquals(2, takePointsIfGoal(oneRow2Colors, shelf));

        // Add another color.
        shelf.insertTiles(List.of(Pair.of(0, 2)), List.of(Tiles.GREEN), true);

        // TEST: Now bottom row has 3 colors (max) -> 1 points; 0.
        Assert.assertEquals(1, takePointsIfGoal(oneRow2Colors, shelf));
        Assert.assertEquals(0, takePointsIfGoal(oneRow2Colors, shelf));
        Assert.assertEquals(0, takePointsIfGoal(oneRow2Colors, shelf));
    }

    @Test
    public void testCheckGoalColumns() {
        var shelf = ShelfTest.shelf2();

        var oneRow2Colors = new CommonGoal(
                CommonGoalPredicate.Columns(2, 3, 1),
                List.of(1, 2, 3)
        );

        // TEST: Bottom column is not complete -> 0 points.
        Assert.assertEquals(0, takePointsIfGoal(oneRow2Colors, shelf));

        // Complete the first column.
        shelf.insertTiles(
                List.of(Pair.of(3, 0), Pair.of(4, 0), Pair.of(5, 0)),
                List.of(Tiles.BLUE, Tiles.BLUE, Tiles.BLUE)
        );

        // TEST: Now first column is complete -> 3 points; 2 points.
        Assert.assertEquals(3, takePointsIfGoal(oneRow2Colors, shelf));
        Assert.assertEquals(2, takePointsIfGoal(oneRow2Colors, shelf));

        // Add another color.
        shelf.insertTiles(List.of(Pair.of(1, 0)), List.of(Tiles.GREEN), true);

        // TEST: Now first column has 3 colors (max) -> 1 points; 0.
        Assert.assertEquals(1, takePointsIfGoal(oneRow2Colors, shelf));
        Assert.assertEquals(0, takePointsIfGoal(oneRow2Colors, shelf));
        Assert.assertEquals(0, takePointsIfGoal(oneRow2Colors, shelf));
    }

    @Test
    public void testCheckGoalScatter() {
        var shelf = ShelfTest.shelf2();
        var goal = new CommonGoal(
                CommonGoalPredicate.Scatter(7),
                List.of(1, 2, 3)
        );

        // TEST: 6 purples -> 0 points.
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));

        // Add 2 purples in different group.
        shelf.insertTiles(
                List.of(Pair.of(0, 4), Pair.of(1, 4)),
                List.of(Tiles.PURPLE, Tiles.PURPLE)
        );

        // TEST: Now 8 purples total -> 3 points; 2 points.
        Assert.assertEquals(3, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(2, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(1, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));
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
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));

        // Complete the shape.
        shelf.insertTiles(List.of(Pair.of(1, 2)), List.of(Tiles.PURPLE), true);

        // TEST: Now shape is complete -> 3 points; 2 points.
        Assert.assertEquals(3, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(2, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(1, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));
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
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));

        // Complete the stairs by adding yellow back in.
        shelf.insertTiles(List.of(Pair.of(0, 4)), List.of(Tiles.YELLOW));

        // TEST: Now stairs are complete -> 3 points; 2 points.
        Assert.assertEquals(3, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(2, takePointsIfGoal(goal, shelf));

        // Add another tile.
        shelf.insertTiles(List.of(Pair.of(1, 4)), List.of(Tiles.YELLOW));

        // TEST: Stairs are still there -> 1 points; 0.
        Assert.assertEquals(1, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));

        // Invert stairs.
        shelf.reflect();

        // TEST: Stairs are still there -> 3 points; 2; 0.
        goal.rechargePoints(List.of(2, 3));
        Assert.assertEquals(3, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(2, takePointsIfGoal(goal, shelf));
        Assert.assertEquals(0, takePointsIfGoal(goal, shelf));
    }

    // Testiamo la funzione "all".
    @Test
    public void testAllRandomDraw() {
        Shelf shelf = ShelfTest.shelf2();

        // Get all goals and shuffle them. Then check if checkGoal returns correct points.
        var allGoals = CommonGoal.all(4);
        Collections.shuffle(allGoals);
        for (var goal : allGoals) {
            Assert.assertTrue(List.of(2, 4, 6, 8, 0).contains(takePointsIfGoal(goal, shelf)));
        }
    }
}
