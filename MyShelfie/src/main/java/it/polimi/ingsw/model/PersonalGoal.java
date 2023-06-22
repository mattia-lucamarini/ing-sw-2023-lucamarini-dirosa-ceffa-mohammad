package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalGoal implements Goal, Serializable {
    /**
     * Method PersonalGoal
     * @author Shaffaeet Mohammad
     * @param constraint given tiles to be placed in given positions in shelf to achieve goal
     * @param points points progressively earned as more tiles are placed
     * Constructs a persona goal using the info provided.
     * */
    public PersonalGoal(HashMap<Pair<Integer, Integer>, Tiles> constraint, List<Integer> points) {
        this.constraint = constraint;
        this.points = points;
    }

    /**
     * Method getConstraint
     * @author Shaffaeet Mohammad
     * Return constraint.
     * */
    @Override
    public HashMap<Pair<Integer, Integer>, Tiles> getConstraint() {
        return constraint;
    }

    /**
     * Method checkGoal
     * @author Shaffaeet Mohammad
     * @param shelf shelf within which the constraint will be checked.
     * Checks if the goal has been achieved within the given shelf and returns the amount of points earned.
     * */
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

    /**
     * Method all
     * @author Shaffaeet Mohammad
     * Generates and returns all personal goal cards that are present in game.
     * */
    // TODO: Make the point lists vary with the number of players in the game.
    public static List<PersonalGoal> all() {
        var allPatterns = List.of(
                new HashMap<>(Map.of(
                        Pair.of(5, 0), Tiles.PURPLE, Pair.of(5, 2), Tiles.BLUE,   Pair.of(4, 4), Tiles.GREEN,
                        Pair.of(3, 3), Tiles.WHITE,  Pair.of(2, 1), Tiles.YELLOW, Pair.of(0, 2), Tiles.LIGHTBLUE)),
                new HashMap<>(Map.of(
                        Pair.of(3, 0), Tiles.GREEN, Pair.of(4, 1), Tiles.PURPLE,   Pair.of(3, 2), Tiles.YELLOW,
                        Pair.of(2, 4), Tiles.WHITE,  Pair.of(1, 3), Tiles.LIGHTBLUE, Pair.of(0, 4), Tiles.BLUE)),
                new HashMap<>(Map.of(
                        Pair.of(4, 0), Tiles.BLUE, Pair.of(4, 3), Tiles.YELLOW,   Pair.of(3, 2), Tiles.PURPLE,
                        Pair.of(2, 1), Tiles.GREEN,  Pair.of(2, 4), Tiles.LIGHTBLUE, Pair.of(0, 0), Tiles.WHITE)),
                new HashMap<>(Map.of(
                        Pair.of(5, 4), Tiles.YELLOW, Pair.of(3, 0), Tiles.LIGHTBLUE,   Pair.of(3, 2), Tiles.BLUE,
                        Pair.of(2, 3), Tiles.PURPLE,  Pair.of(1, 1), Tiles.WHITE, Pair.of(1, 2), Tiles.GREEN)),
                new HashMap<>(Map.of(
                        Pair.of(4, 1), Tiles.LIGHTBLUE, Pair.of(2, 1), Tiles.BLUE,   Pair.of(2, 2), Tiles.WHITE,
                        Pair.of(1, 4), Tiles.PURPLE,  Pair.of(0, 0), Tiles.YELLOW, Pair.of(0, 3), Tiles.GREEN)),
                new HashMap<>(Map.of(
                        Pair.of(5, 2), Tiles.LIGHTBLUE, Pair.of(5, 4), Tiles.GREEN,   Pair.of(3, 3), Tiles.WHITE,
                        Pair.of(1, 1), Tiles.YELLOW,  Pair.of(1, 3), Tiles.BLUE, Pair.of(0, 0), Tiles.PURPLE)),
                new HashMap<>(Map.of(
                        Pair.of(5, 0), Tiles.GREEN, Pair.of(4, 3), Tiles.BLUE,   Pair.of(3, 1), Tiles.PURPLE,
                        Pair.of(2, 0), Tiles.LIGHTBLUE,  Pair.of(1, 4), Tiles.YELLOW, Pair.of(0, 2), Tiles.WHITE)),
                new HashMap<>(Map.of(
                        Pair.of(5, 4), Tiles.BLUE, Pair.of(4, 1), Tiles.GREEN,   Pair.of(3, 2), Tiles.LIGHTBLUE,
                        Pair.of(2, 0), Tiles.PURPLE,  Pair.of(1, 3), Tiles.WHITE, Pair.of(0, 3), Tiles.YELLOW)),
                new HashMap<>(Map.of(
                        Pair.of(5, 2), Tiles.YELLOW, Pair.of(3, 2), Tiles.GREEN,   Pair.of(2, 4), Tiles.WHITE,
                        Pair.of(1, 1), Tiles.LIGHTBLUE,  Pair.of(1, 4), Tiles.PURPLE, Pair.of(0, 0), Tiles.BLUE)),
                new HashMap<>(Map.of(
                        Pair.of(5, 4), Tiles.LIGHTBLUE, Pair.of(4, 1), Tiles.YELLOW,   Pair.of(3, 0), Tiles.WHITE,
                        Pair.of(2, 3), Tiles.GREEN,  Pair.of(1, 1), Tiles.BLUE, Pair.of(0, 3), Tiles.PURPLE)),
                new HashMap<>(Map.of(
                        Pair.of(5, 2), Tiles.PURPLE, Pair.of(4, 1), Tiles.WHITE,   Pair.of(3, 0), Tiles.YELLOW,
                        Pair.of(2, 2), Tiles.BLUE,  Pair.of(1, 4), Tiles.GREEN, Pair.of(0, 3), Tiles.LIGHTBLUE)),
                new HashMap<>(Map.of(
                        Pair.of(5, 2), Tiles.WHITE, Pair.of(4, 1), Tiles.PURPLE,   Pair.of(3, 2), Tiles.BLUE,
                        Pair.of(0, 3), Tiles.LIGHTBLUE,  Pair.of(1, 4), Tiles.YELLOW, Pair.of(0, 0), Tiles.GREEN))
        );

        var ret = new ArrayList<PersonalGoal>();
        for (var pattern : allPatterns) {
            var points = List.of(1, 2, 4, 6, 9, 12);
            ret.add(new PersonalGoal(pattern, points));
        }

        return ret;
    }

    private final HashMap<Pair<Integer, Integer>, Tiles> constraint;
    private final List<Integer> points;
}
