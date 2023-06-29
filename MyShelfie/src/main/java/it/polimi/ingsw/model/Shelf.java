package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class: Shelf
 * @author Mohammad Shaffaeet
 * This class is an abstraction of the Shelf. The choordinates (0, 0) indicate the bottom left corner.
 * The shelf has 6 rows and 5 columns. Tiles can only be placed in valid cells, which are only the bottom row when
 * the shelf is initialized.
 */
public class Shelf implements Serializable {
    public static final int ROWS = 6;
    public static final int COLUMNS = 5;

    /**
     * Method: Shelf
     * Construct an empty shelf, validating the bottom row for tile placement.
     */
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

    public boolean isCellEmpty(int x, int y) {
        return matrix[x][y].isEmpty();
    }

    public boolean isCellValid(int x, int y) {
        return matrix[x][y] == Tiles.VALID;
    }

    /**
     * Method: insertTiles
     *
     * @param positions list of positions to place the tiles
     * @param colors    colors of tiles that are placed (must be the same length as "positions")
     *                  This method places tiles on the shelf. It throws an exception if the tiles aren't placed in a valid cell.
     *                  It transforms the cells above the tiles that are placed into valid cells.
     */
    public void insertTiles(List<Pair<Integer, Integer>> positions, List<Tiles> colors) {
        insertTiles(positions, colors, false);
    }

    public void insertTiles(List<Pair<Integer, Integer>> positions, List<Tiles> colors, boolean testMode) {
        List<Tiles> oldValues = new ArrayList<>();
        if (positions.size() != colors.size()) {
            throw new RuntimeException("Size mismatch.");
        }
        //System.out.println("1");
        if(positions.get(0).getFirst()!=0 && getTile((positions.get(0).getFirst()-1),positions.get(0).getSecond()).equals(Tiles.VALID)){
            throw new UnsupportedOperationException("Tiles must be adjacent");
        }
        //System.out.println("2");

        if(positions.get(0).getFirst()!=0 && getTile((positions.get(0).getFirst()-1),positions.get(0).getSecond()).equals(Tiles.NOTVALID)){
            throw new UnsupportedOperationException("Tiles must be adjacent");
        }
        //System.out.println("3");

        for (int i = 1; i < positions.size(); i++) {//SAME COLUMN TEST
            if (!(positions.get(i).getSecond().equals(positions.get(i - 1).getSecond()))) {
                throw new UnsupportedOperationException("All tiles need to be on the same column.");
            }

        }
        //System.out.println("4");

        for (int i = 1; i < positions.size(); i++) {//ADJACENT COLUMN TEST
            if (!(positions.get(i).getFirst().equals(positions.get(i - 1).getFirst()+1))) {
                throw new UnsupportedOperationException("All tiles in the same column must be adjacent");
            }

        }
        //System.out.println("5");

        for (int i = 0; i < positions.size(); i++) {
            int x = positions.get(i).getFirst();
            int y = positions.get(i).getSecond();
            if (!testMode && !isCellValid(x, y)) {
                //System.out.println("putitback");
                putitback(positions, oldValues);
                throw new RuntimeException("Invalid position (" + x + ", " + y + ")");
            }

            oldValues.add(getTile(x,y));
            matrix[x][y] = colors.get(i);
            if (!testMode && x < 5)
                matrix[x + 1][y] = Tiles.VALID;
        }
        //System.out.println("6");

    }

    public void putitback(List<Pair<Integer, Integer>> oldpositions,List<Tiles> old){
        //System.out.println("in putitback");

        for(int i=0 ; i<oldpositions.size(); ++i){
            int x = oldpositions.get(i).getFirst();
            int y = oldpositions.get(i).getSecond();
            matrix[x][y] = old.get(i);
        }
    }


    /**
     * Method: removeTiles
     * @param col Column from which to remove tiles.
     * @param amount Amount of tiles to remove.
     * Remove tiles from given column. Keeps the board valid.
     */
    public void removeTiles(int col, int amount) {
        for (int r = Shelf.ROWS - 1; r >= 0 && amount > 0; r--) {
            if (!isCellEmpty(r, col)) {
                amount--;
                matrix[r][col] = Tiles.VALID;
                if (r < Shelf.ROWS - 1)
                    matrix[r + 1][col] = Tiles.NOTVALID;
            }
        }
    }

    /**
     * Reflects shelf matrix along horizontal axis.
     */
    public void reflect() {
        var newMatrix = new Tiles[ROWS][COLUMNS];

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                newMatrix[r][COLUMNS - 1 - c] = matrix[r][c];
            }
        }

        matrix = newMatrix;
    }

    /**
     * Method: findTileGroups
     * Returns a list of all groups of adjacent tiles of the same color (specifying color and number of tiles for each
     * group). Uses a flooding algorithm.
     * */
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
                    Tiles colorOfGroup = matrix[r][c];
                    int nOfTilesInGroup = 0;
                    // Start recursive call (flooding)
                    nOfTilesInGroup = findTileGroup(r, c, colorOfGroup, visited, nOfTilesInGroup);
                    // Collect group inside list of all groups
                    groups.add(Pair.of(colorOfGroup, nOfTilesInGroup));
                }
            }
        }

        return groups;
    }

    /**
     * Method: findTileGroup
     * @param r current row.
     * @param c current column.
     * @param groupColor color of group that is currently being flooded.
     * @param visited matrix of place that have already been visited by flooding (both current and previous).
     * @param count counter keeping track of flooded in-group tiles.
     * Helper function used in "findTileGroups" to start tile floodings. Floods by recursively expanding in a cross pattern.
     * Stops when: different color is encountered or out bounds or tile has already been visited. Meanwhile, records the
     * number of flooded tiles across recursive calls in "count". At the end of recursion, this number will correspond
     * with the number of tiles in the group that contains the tile given to the initial call.
     */
    private int findTileGroup(int r, int c, Tiles groupColor, boolean[][] visited, int count) {
        // Return number of tiles within the group
        // Ferma espansione quando usciamo fuori da matrice
        if (r < 0 || r >= 6){
            return count;
        }
        else if (c < 0 || c >= 5) {
            return count;
        }
        // Ferma espansione quando colore non è del gruppo (di casella iniziale)
        else if (matrix[r][c] != groupColor) {
            return count;
        }
        // Posizione già visitata (ferma espansione)
        else if (visited[r][c] == true) {
            return count;
        }
        // Visita + conta + espansione
        else {
            // Segna casella come già visitata
            visited[r][c] = true;

            // Aggiungiamo 1 al numero di caselle nel gruppo
            count++;

            // Procedi con espansione a croce (esce anche da matrice ma bloccata sopra)
            // . # .
            // # 0 #
            // . # .
            count = findTileGroup(r + 1, c, groupColor, visited, count);
            count = findTileGroup(r, c + 1, groupColor, visited, count);
            count = findTileGroup(r - 1, c, groupColor, visited, count);
            count = findTileGroup(r, c - 1, groupColor, visited, count);

            return count;
        }
    }

    public static int scoreGroup(Pair<Tiles, Integer> group) {
        int gainedPoints = 0;
        if (group.getSecond() == 3)
            gainedPoints = 2;
        else if (group.getSecond() == 4)
            gainedPoints = 3;
        else if (group.getSecond() == 5)
            gainedPoints = 5;
        else if (group.getSecond() >= 6)
            gainedPoints = 8;
        return gainedPoints;
    }

    public void printShelf(){
        for(int i = ROWS-1 ; i >= 0; --i){
            System.out.print(i + " ");
            for(int j = 0; j < COLUMNS; ++j){
                if (matrix[i][j] == Tiles.NOTVALID)
                    System.out.print(" ");
                else if (matrix[i][j] == Tiles.VALID)
                    System.out.print("-");
                else
                    System.out.print(matrix[i][j].toString().charAt(0));
                System.out.print(" ");
            }
            System.out.print("\n");
        }
        System.out.print("  ");
        for (int i = 0; i < COLUMNS; i++)
            System.out.print(i + " ");
        System.out.print("\n");
    }

    public Tiles getTile(int r, int c) {
        return matrix[r][c];
    }

    private Tiles[][] matrix;
    private int totalTiles;

}

