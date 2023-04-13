package it.polimi.ingsw;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
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

    @Override
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

    public static Predicate<Shelf> Adjacent(int nOfGroups, int nOfTiles) {
        return (Shelf shelf) -> {
            // Count how many groups of "nOfTiles" tiles are there.
            int count = 0;
            for (Pair<Tile, Integer> group : shelf.findTileGroups()) {
                if (group.getSecond() == nOfTiles) count++;
            }

            // Constraint is reached if they reach "nOfTiles" (or more).
            return count >= nOfGroups;
        };
    }

    public static Predicate<Shelf> FourCorners() {
        return (Shelf shelf) -> {
            return shelf.getTile(0, 0) == shelf.getTile(0, Shelf.COLUMNS) &&
                    shelf.getTile(0, 0) == shelf.getTile(Shelf.ROWS, 0) &&
                    shelf.getTile(0, 0) == shelf.getTile(Shelf.ROWS, Shelf.COLUMNS);
        };
    }

    public static Predicate<Shelf> Stairs() {
        Predicate<Shelf> isDescending = (Shelf shelf) -> {
            for (int col = 0; col < Shelf.COLUMNS; col++) {
                for (int row = 0; row < Shelf.COLUMNS - col; row++) {
                    if (shelf.isCellEmpty(row, col)) {
                        return false;
                    }
                }
            }
            return true;
        };

        Predicate<Shelf> isAscending = (Shelf shelf) -> {
            for (int col = 0; col < Shelf.COLUMNS; col++) {
                for (int row = 0; row < col + 1; row++) {
                    if (shelf.isCellEmpty(row, col)) {
                        return false;
                    }
                }
            }
            return true;
        };

        return isDescending.or(isAscending);
    }

    public static Predicate<Shelf> Rows(int minColors, int maxColors, int nOfGroups) {
        // Rows3Colors -> Rows(3, 6, 4)
        // RowsAllDiff -> Rows(6, 6, 2)
        return (Shelf shelf) -> {
            int count = 0;
            for (int row = 0; row < Shelf.ROWS; row++) {
                var colors = new HashSet<Tile>();
                for (int col = 0; col < Shelf.COLUMNS; col++) {
                    colors.add(shelf.getTile(row, col));
                }
                if (minColors <= colors.size() && colors.size() >= maxColors) {
                    count++;
                }
            }

            return count >= nOfGroups;
        };
    }

    public static Predicate<Shelf> Columns(int minColors, int maxColors, int nOfGroups) {
        // Cols3Colors -> Columns(3, 6, 4)
        // ColsAllDiff -> Columns(6, 6, 2)
        return (Shelf shelf) -> {
            int count = 0;
            for (int col = 0; col < Shelf.COLUMNS; col++) {
                var colors = new HashSet<Tile>();
                for (int row = 0; row < Shelf.ROWS; row++) {
                    colors.add(shelf.getTile(row, col));
                }
                if (minColors <= colors.size() && colors.size() >= maxColors) {
                    count++;
                }
            }

            return count >= nOfGroups;
        };
    }

    public static Predicate<Shelf> Scatter(int nOfTiles) {
        return (Shelf shelf) -> {
            var colors = new HashMap<Tile, Integer>();
            for (int col = 0; col < Shelf.COLUMNS; col++) {
                for (int row = 0; row < Shelf.ROWS; row++) {
                    var color = shelf.getTile(row, col);
                    var count = colors.getOrDefault(color, 0);
                    colors.put(shelf.getTile(row, col), count + 1);
                }
            }

            for (var color : colors.entrySet()) {
                if (color.getValue() >= nOfTiles) {
                    return true;
                }
            }

            return false;
        };
    }

    public static Predicate<Shelf> Shape(List<Pair<Integer, Integer>> shape, int nOfGroups) {
        return (Shelf shelf) -> {
            int count = 0;
            for (int row = 0; row < Shelf.ROWS; row++) {
                for (int col = 0; col < Shelf.COLUMNS; col++) {
                    var color = shelf.getTile(row, col);
                    boolean shapeFound = false;
                    for (var pos : shape) {
                        var sr = row + pos.getFirst();
                        var sc = col + pos.getSecond();
                        if (shelf.getTile(sr, sc) != color) {
                            shapeFound = true;
                            break;
                        }
                    }
                    if (shapeFound) count++;
                }
            }

            return count >= nOfGroups;
        };
    }

    private final Predicate<Shelf> constraint;
    private Stack<Integer> points;
}
