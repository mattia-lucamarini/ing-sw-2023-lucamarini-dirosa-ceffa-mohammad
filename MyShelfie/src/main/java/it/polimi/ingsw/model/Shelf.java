package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class: Shelf
 * @author Mohammad Shaffaeet
 * This class is an abstraction of the Shelf. The choordinates (0, 0) indicate the bottom left corner.
 * The shelf has 6 rows and 5 columns. Tiles can only be placed in valid cells, which are only the bottom row when
 * the shelf is initialized.
 */
public class Shelf {
    public static final int ROWS = 6;
    public static final int COLUMNS = 5;

    public Shelf() {
        // Initialize matrix. Initially, you can only place tiles in bottom row (0).
        matrix = new Tiles[6][5];
        for (int col = 0; col < 5; col++) {
            matrix[0][col] = Tiles.VALID;
        }
        for (int row = 1; row < 6; row++) {
            for (int col = 0; col < 5; col++) {
                matrix[row][col] = Tiles.NOTVALID;
            }
        }

        // Initialize totalTiles
        totalTiles = 0;
    }

    public boolean isCellEmpty(int x, int y){
        return matrix[x][y] == Tiles.NOTVALID || matrix[x][y] == Tiles.VALID;
    }

    public boolean isCellValid(int x, int y) {
        return matrix[x][y] == Tiles.VALID;
    }

    /**
     * Method: insertTiles
     * @param positions list of positions to place the tiles
     * @param colors colors of tiles that are placed (must be the same length as "positions")
     * This method places tiles on the shelf. It throws an exception if the tiles aren't placed in a valid cell.
     * It transforms the cells above the tiles that are placed into valid cells.
     * */
    public void insertTiles(List<Pair<Integer, Integer>> positions, List<Tiles> colors) {
        if (positions.size() != colors.size()) {
            throw new RuntimeException("Size mismatch.");
        }

        for (int i = 0; i < positions.size(); i++) {
            int x = positions.get(i).getFirst();
            int y = positions.get(i).getSecond();

            if (!isCellValid(x, y)) {
                throw new RuntimeException("Can't insert tile without other tiles underneath.");
            }
            else {
                matrix[x][y] = colors.get(i);
                if (x < 5) {
                    matrix[x + 1][y] = Tiles.VALID;
                }
            }
        }
    }

    public List<Pair<Tiles, Integer>> findTileGroups() {
        List<Pair<Tiles, Integer>> groups = new ArrayList<>();
        boolean[][] visited = new boolean[6][5];

        // Init visited matrix to all false
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {
                visited[r][c] = false;
            }
        }

        // Find all groups
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 5; c++) {
                if (!visited[r][c] && !isCellEmpty(r, c)){
                    int groupN = findTileGroup(r, c, matrix[r][c], visited); // call1
                    groups.add(Pair.of(matrix[r][c], groupN));
                }
            }
        }

        return groups;
    }

    private int findTileGroup(int r, int c, Tiles color, boolean[][] visited) {
        if (r < 0 || r >= 6) return 0;
        if (c < 0 || c >= 5) return 0;
        if (matrix[r][c] != color) return 0;
        if (visited[r][c]) return 0;

        visited[r][c] = true;

        int n = 1;

        n += findTileGroup(r + 1, c, color, visited);
        n += findTileGroup(r, c + 1, color, visited);
        n += findTileGroup(r - 1, c, color, visited);
        n += findTileGroup(r, c - 1, color, visited);

        return n;
    }

    public Tiles getTile(int r, int c) {
        return matrix[r][c];
    }

    private Tiles[][] matrix;
    private int totalTiles;

}
