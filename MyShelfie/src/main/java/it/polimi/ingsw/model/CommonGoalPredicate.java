package it.polimi.ingsw.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class CommonGoalPredicate {
    public static Predicate<Shelf> Adjacent(int nOfGroups, int nOfTiles) {
        return (Shelf shelf) -> {
            // Count how many groups of "nOfTiles" tiles are there.
            int count = 0;
            for (Pair<Tiles, Integer> group : shelf.findTileGroups()) {
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
                var colors = new HashSet<Tiles>();
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

    /**
     * Method: Columns
     * @param minColors minimum number of different colors
     * @param maxColors maximum number of different colors
     * @param nOfGroups minimum number of required columns with required color groups to obtain bonus
     * This method creates a predicate that searches for "nOfGroups" or more columns that contain a number of colors within
     * "minColors" and "maxColors".
     * */
    public static Predicate<Shelf> Columns(int minColors, int maxColors, int nOfGroups) {
        // ColsMax3Colors -> Columns(1, 3, 3)
        // ColsAllDiff    -> Columns(6, 6, 2)
        return (Shelf shelf) -> {
            int count = 0;
            for (int col = 0; col < Shelf.COLUMNS; col++) {
                var colors = new HashSet<Tiles>();
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

    /**
     * Method: Scatter
     * @param nOfTiles minimum number of required tiles to obtain bonus
     * This method creates a predicate that searches for "nOfTiles" or more of the same color within the shelf.
     * */
    public static Predicate<Shelf> Scatter(int nOfTiles) {
        // Scatter8 -> Scatter(8)
        return (Shelf shelf) -> {
            var colors = new HashMap<Tiles, Integer>();
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


    /**
     * Method: Shape
     * @param shape list of positions that make the shape that is searched
     * @param width width of the shape
     * @param height height of the shape
     * @param nOfGroups minimum number of shapes required for bonus
     * This method creates a predicate that searches for "nOfGroups" or more of the given shape within the shelf. It
     * is important to define the shape with (0,0) being the bottom left cell.
     * */
    public static Predicate<Shelf> Shape(List<Pair<Integer, Integer>> shape, int width, int height, int nOfGroups) {
        // TwoSquares -> Shape(List.of(Pair.of(0, 0), Pair.of(0, 1), Pair.of(1, 0), Pair.of(1, 1)), 2, 2, 2)
        // Cross      -> Shape(List.of((0, 0), (1, 1), (2, 2), (2, 0), (0, 2)), 3, 3, 1)
        // Diagonals  -> Shape(List.of((0, 0), (1, 1), (2, 2), ...)), 5, 5, 1).or(Shape(List.of((5, 0), ())), 5, 5, 1)
        return (Shelf shelf) -> {
            int count = 0;
            for (int row = 0; row <= Shelf.ROWS - height; row++) {
                for (int col = 0; col <= Shelf.COLUMNS - width; col++) {
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
}
