package it.polimi.ingsw.model;

import java.util.*;
import java.util.function.Predicate;

public class CommonGoal implements Goal {
    public CommonGoal(Predicate<Shelf> constraint, Stack<Integer> points) {
        this.constraint = constraint;
        this.points = points;
    }

    @Override
    public Object getConstraint() {
        return constraint;
    }

    public int checkGoal(Shelf shelf) {
        if (constraint.test(shelf)) {
            // Return the next point bonus on the stack.
            return points.pop();
        }
        else {
            // Constraint not reached.
            return 0;
        }
    }

    /**
     * Method: all
     * This method creates and returns all 12 common goals that are present in the game, giving each an independent point
     * stack and predicate.
     * */
    public static List<CommonGoal> all() {
        var square = List.of(
                Pair.of(0, 0), Pair.of(0, 1), Pair.of(1, 0), Pair.of(1, 1)
        );
        var forwardDiagonal = List.of(
                Pair.of(0, 0), Pair.of(1, 1), Pair.of(2, 2), Pair.of(3, 3), Pair.of(4, 4)
        );
        var backDiagonal = List.of(
                Pair.of(4, 0), Pair.of(3, 1), Pair.of(2, 2), Pair.of(1, 3), Pair.of(0, 4)
        );
        var cross = List.of(
                Pair.of(0, 0), Pair.of(1, 1), Pair.of(2, 2), Pair.of(0, 2), Pair.of(2, 0)
        );

        var allPredicates = List.of(
                CommonGoalPredicate.Adjacent(6, 2),                // Adjacent6x2
                CommonGoalPredicate.FourCorners(),                                 // FourCorners
                CommonGoalPredicate.Adjacent(4, 4),               // Adjacent4x4
                CommonGoalPredicate.Shape(square, 2, 2, 2),  // Square
                CommonGoalPredicate.Columns(1, 3, 3),  // Columns3x3
                CommonGoalPredicate.Scatter(8),
                CommonGoalPredicate.Shape(forwardDiagonal, 5, 5, 1)
                        .or(CommonGoalPredicate.Shape(backDiagonal, 5, 5, 1)),
                CommonGoalPredicate.Rows(1, 3, 4),
                CommonGoalPredicate.Columns(6, 6, 2),
                CommonGoalPredicate.Rows(6, 6, 2),
                CommonGoalPredicate.Shape(cross, 3, 3, 1),
                CommonGoalPredicate.Stairs()
        );

        var ret = new ArrayList<CommonGoal>();
        for (var pred : allPredicates) {
            var stack = new Stack<Integer>();
            stack.addAll(List.of(8, 6, 4));
            ret.add(new CommonGoal(pred, stack));
        }

        return ret;
    }

    private final Predicate<Shelf> constraint;
    private Stack<Integer> points;
}
