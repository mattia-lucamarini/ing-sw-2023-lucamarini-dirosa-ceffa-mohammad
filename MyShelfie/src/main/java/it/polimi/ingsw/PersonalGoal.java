package it.polimi.ingsw;

import java.util.HashMap;
import java.util.List;

public class PersonalGoal implements Goal {
    public PersonalGoal(HashMap<Pair<Integer, Integer>, Tiles> constraint, List<Integer> points) {
        this.constraint = constraint;
        this.points = points;
    }

    @Override
    public Object getConstraint() {
        return constraint;
    }

    @Override
    public int checkGoal(Shelf shelf) {
        int count = 0;
        for (var entry : constraint.entrySet()) {
            int x = entry.getKey().getFirst();
            int y = entry.getKey().getSecond();

            if (shelf.getTile(x, y) == entry.getValue()) {
                // Tile is of the same color
                count++;
            }
        }

        return count == 0 ? 0 : points.get(count - 1);
    }

    private final HashMap<Pair<Integer, Integer>, Tiles> constraint;
    private final List<Integer> points;
}
