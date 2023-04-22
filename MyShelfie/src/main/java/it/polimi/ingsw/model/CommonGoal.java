package it.polimi.ingsw.model;

import java.util.*;
import java.util.function.Predicate;

public class CommonGoal implements Goal {
    /**
     * Method PersonalGoal
     * @author Shaffaeet Mohammad
     * @param constraint given tiles to be placed in given positions in shelf to achieve goal
     * @param points stack of points earned. First amount popped everytime the constraint is reached.
     * Constructs a common goal using the info provided.
     * */
    public CommonGoal(Predicate<Shelf> constraint, Stack<Integer> points) {
        this.constraint = constraint;
        this.points = points;
    }

    /**
     * Method getConstraint
     * @author Shaffaeet Mohammad
     * Return constraint.
     * */
    @Override
    public Object getConstraint() {
        return constraint;
    }

    /**
     * Method checkGoal
     * @author Shaffaeet Mohammad
     * @param shelf shelf within which the constraint will be checked.
     * Checks if the goal has been achieved within the given shelf and returns the amount of points earned.
     * */
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
                // Adjacent6x2
                CommonGoalPredicate.Adjacent(6, 2),
                // FourCorners
                CommonGoalPredicate.FourCorners(),
                // Adjacent4x4
                CommonGoalPredicate.Adjacent(4, 4),
                // Square
                CommonGoalPredicate.Shape(square, 2, 2, 2),
                // Columns3x3
                CommonGoalPredicate.Columns(1, 3, 3),
                // Scatter8
                CommonGoalPredicate.Scatter(8),
                // Diagonals
                CommonGoalPredicate.Shape(forwardDiagonal, 5, 5, 1)
                        .or(CommonGoalPredicate.Shape(backDiagonal, 5, 5, 1)),
                // ThreeColors4Rows
                CommonGoalPredicate.Rows(1, 3, 4),
                // SixColors2Columns
                CommonGoalPredicate.Columns(6, 6, 2),
                // SixColors2Rows
                CommonGoalPredicate.Rows(6, 6, 2),
                // Cross
                CommonGoalPredicate.Shape(cross, 3, 3, 1),
                // Stairs
                CommonGoalPredicate.Stairs()
        );

        // Associate all goal predicates with a stack of points.
        // TODO: Vary w/ number of players
        var ret = new ArrayList<CommonGoal>(); // empty
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
