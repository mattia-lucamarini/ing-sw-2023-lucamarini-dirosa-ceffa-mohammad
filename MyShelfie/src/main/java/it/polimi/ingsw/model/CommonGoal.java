package it.polimi.ingsw.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

public class CommonGoal implements Goal {
    public CommonGoal(CommonGoalType type, Stack<Integer> points) {
        this.type = type;
        this.points = points;
    }

    @Override
    public Object getConstraint() {
        return type;
    }

    @Override
    public int checkGoal(Shelf shelf) {
        // TODO: use type enum for behaviour
        return 0;
    }

    private final CommonGoalType type;
    private Stack<Integer> points;
}
