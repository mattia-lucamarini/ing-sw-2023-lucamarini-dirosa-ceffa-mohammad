package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class PersonaGoalTest {
    @Test
    public void testCheckGoal() {
        // 0: 0
        // 1: 1
        // 2: 0
        // ...
        // 9: 2
        // 10: 0
        // ...
        Shelf shelf = ShelfTest.stairsShelf();
        var allGoals = PersonalGoal.all();

        // Asterisks are matches.
        // Shelf       Goal0       Goal1
        // G . . . .   P . B . .   . . . . .
        // G P . . .   . . . . G   . P*. . .
        // G P B . .   . . . W .   G*. Y . .
        // P P B . .   . Y . . .   . . . . W
        // P P B B .   . . . . .   . . . L .
        // P P P B Y   . . L . .   . . . . B
        Assert.assertEquals(0, allGoals.get(0).checkGoal(shelf));
        Assert.assertEquals(2, allGoals.get(1).checkGoal(shelf));
    }

    @Test
    public void testAllRandomDraw() {
        Shelf shelf = ShelfTest.shelf2();

        // Get all goals and shuffle them. Then check if checkGoal returns correct points.
        var allGoals = PersonalGoal.all();
        Collections.shuffle(allGoals);
        int i = 0;
        for (var goal : allGoals) {
            var score = goal.checkGoal(shelf);
            System.out.printf("%d: %d\n", i, score);
            Assert.assertTrue(List.of(0, 1, 2, 4, 6, 9, 12).contains(score));
            i++;
        }
    }
}
