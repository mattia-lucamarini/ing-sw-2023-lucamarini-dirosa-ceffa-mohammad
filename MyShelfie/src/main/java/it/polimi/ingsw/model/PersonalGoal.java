package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalGoal implements Goal, Serializable {
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

    public static List<PersonalGoal> all() {
        // TODO: Comprare gioco per capire le vere pattern delle carte personal gaol come sono fatte
        var allPatterns = List.of(
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(Pair.of(0, 0), Tiles.PURPLE, Pair.of(1, 0), Tiles.PURPLE))
        );

        var ret = new ArrayList<PersonalGoal>();
        for (var pattern : allPatterns) {
            // TODO: Capire se sono diversi i punti per ogni carta o no.
            var points = List.of(1, 2, 4, 6, 9, 12);
            ret.add(new PersonalGoal(pattern, points));
        }

        return ret;
    }

    private final HashMap<Pair<Integer, Integer>, Tiles> constraint;
    private final List<Integer> points;
}
