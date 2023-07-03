package it.polimi.ingsw.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLOutput;
import java.util.*;

/**
 * Class: Board
 * @author Angelo Di Rosa
 * This class is an abstraction og the physical board where the game takes place.
 * It consists in a 9x9 matrix of Cells that can available (or not) for the game.
 */
public class Board implements Serializable {

    private Cell[][] grid = new Cell[9][9];
    Bag bag= new Bag();
    /**Method Constructor
     * The constructor initialize the Board by putting a VALID/NOTVALID value on the cell depending on if the cell is used for the game on not and also
     * depending on the number of players*/
    public Board(int numplayers){
        for(int i = 0 ; i < 9 ; ++i){
            for (int j = 0; j < 9 ; ++j){
                grid[i][j] = new Cell(Tiles.NOTVALID);
            }
        }
        grid[1][3].assignValue(Tiles.VALID);
        grid[1][4].assignValue(Tiles.VALID);
        grid[2][3].assignValue(Tiles.VALID);
        grid[2][4].assignValue(Tiles.VALID);
        grid[2][5].assignValue(Tiles.VALID);
        grid[3][2].assignValue(Tiles.VALID);
        grid[3][3].assignValue(Tiles.VALID);
        grid[3][4].assignValue(Tiles.VALID);
        grid[3][5].assignValue(Tiles.VALID);
        grid[3][6].assignValue(Tiles.VALID);
        grid[3][7].assignValue(Tiles.VALID);
        grid[4][1].assignValue(Tiles.VALID);
        grid[4][2].assignValue(Tiles.VALID);
        grid[4][3].assignValue(Tiles.VALID);
        grid[4][4].assignValue(Tiles.VALID);
        grid[4][5].assignValue(Tiles.VALID);
        grid[4][6].assignValue(Tiles.VALID);
        grid[4][7].assignValue(Tiles.VALID);
        grid[5][1].assignValue(Tiles.VALID);
        grid[5][2].assignValue(Tiles.VALID);
        grid[5][3].assignValue(Tiles.VALID);
        grid[5][4].assignValue(Tiles.VALID);
        grid[5][5].assignValue(Tiles.VALID);
        grid[5][6].assignValue(Tiles.VALID);
        grid[6][3].assignValue(Tiles.VALID);
        grid[6][4].assignValue(Tiles.VALID);
        grid[6][5].assignValue(Tiles.VALID);
        grid[7][4].assignValue(Tiles.VALID);
        grid[7][5].assignValue(Tiles.VALID);
        if(numplayers >= 3){
            grid[0][3].assignValue(Tiles.VALID);
            grid[2][2].assignValue(Tiles.VALID);
            grid[2][6].assignValue(Tiles.VALID);
            grid[3][8].assignValue(Tiles.VALID);
            grid[5][0].assignValue(Tiles.VALID);
            grid[6][2].assignValue(Tiles.VALID);
            grid[6][6].assignValue(Tiles.VALID);
            grid[8][5].assignValue(Tiles.VALID);
            if(numplayers == 4){
                grid[0][4].assignValue(Tiles.VALID);
                grid[1][5].assignValue(Tiles.VALID);
                grid[3][1].assignValue(Tiles.VALID);
                grid[4][0].assignValue(Tiles.VALID);
                grid[4][8].assignValue(Tiles.VALID);
                grid[5][7].assignValue(Tiles.VALID);
                grid[7][3].assignValue(Tiles.VALID);
                grid[8][4].assignValue(Tiles.VALID);
            }
        }
    }

    public Board(int numplayers, List<Character> initMatrix) {
        for (int i = 0; i < 9 * 9; ++i) {
            int r = i / 9;
            int c = i % 9;

            grid[r][c] = new Cell(Tiles.NOTVALID);

            if (Character.isDigit(initMatrix.get(i))) {
                this.grid[r][c].assignValue(
                        Character.getNumericValue(initMatrix.get(i)) <= numplayers ? Tiles.VALID : Tiles.NOTVALID
                );
            } else {
                this.grid[r][c].assignValue(Tiles.fromChar(initMatrix.get(i)));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Arrays.deepEquals(grid, board.grid) && bag.equals(board.bag);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(bag);
        result = 31 * result + Arrays.deepHashCode(grid);
        return result;
    }

    @Override
    public String toString() {
        return "Board{\n" +
                "grid:\n" + gridToString() + ",\n" +
                "bag=" + bag + "\n" +
                "}";
    }

    public Tiles getTile(int row, int column) {
        return grid[row][column].getTile();
    }

    /**
     * Method : refillBoard
     * @author Angelo Di Rosa
     * refillBoard is used to fill the board with the tiles to start/keep playing the game.
     * It iterates on the board checks if the cell is empty (has a "VALID" value on it).
     * If the cell has a "NOTVALID" value or any other object tile value, refillBoard won't replace it with another tile (as a matter of fact that cell can't be used for the game/a tile is already in place).
     * If the cell has a "VALID" value (which means the empty cell can be used for the game), refillBoard will replace it with another tile.
     * This method calls val.nextInt(6) which generates a random number from 0 to 5. This number is stored and used as a parameter for bag.remainingTiles(r).
     * remainingTiles counts how many tiles of the same type are left. */

    public void refillBoard(){
        Random val = new Random();
        Tiles t;
        for(int i = 0; i < 9 ; ++i){
            for(int j = 0 ; j < 9; ++j){
                if(grid[i][j].isEmpty()){
                    t = Tiles.NOTVALID;
                    while(t == Tiles.NOTVALID){
                        int r = val.nextInt(6);
                        t = bag.remainingTiles(r);
                    }
                    grid[i][j].assignValue(t);
                }
            }
        }
        return;
    }
    /**
     * Method : checkStatus
     * @author Angelo Di Rosa
     * This method checks how many tiles are left on the board (remainingtiles) and counts how many of them are not surrounded by other tiles.
     * If those numbers happen to be equal, it means that the board needs to be refilled (the player can take only single tiles).
     * checkStatus iterates on the board and for every value other than "VALID" and "NOTVALID", calls anyTilesAround()  */
    public boolean checkStatus() {
        int singletiles=0, remainingtiles=0;

        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if(!grid[i][j].isEmpty() && !grid[i][j].isNotValid()){
                    ++remainingtiles;
                    if (!this.anyTilesAround(i, j)){
                        ++singletiles;
                    }
                }
            }
            //System.out.println("remaining tiles: " + remainingtiles + "single tiles:" +singletiles);
        }
        return singletiles == remainingtiles; //The board needs to be refilled.
    }

    /**Method: anyTilesAround(int i, int j)
     * @author Angelo Di Rosa
     * @param i index1
     * @param j index2
     * anyTilesAround checks if the tile is surrounded by other tiles up, down, left and right (north, south, west and east).
     * In order to do so, anyTilesAround just checks if the cells in the north/south/west/east position have neither a 'VALID' value or a 'NOTVALID' value.
     * Object tiles surrounded by only NOTVALID tiles are not allowed in the game!*/

    public boolean anyTilesAround(int i, int j){
        int north, south, east, west;
        north = i-1;
        south = i+1;
        east= j+1;
        west = j-1;
        if(north>=0 && !grid[north][j].isNotValid()){
            if (!grid[north][j].isEmpty()){
                return true;
            }

        }
        if(south<=8 && !grid[south][j].isNotValid()){
            if(!grid[south][j].isEmpty()){
                return true;
            }
        }
        if(west>=0 && !grid[i][west].isNotValid()){
            if(!grid[i][west].isEmpty()) {
                return true;
            }
        }
        if(east<=8 && !grid[i][east].isNotValid()){
            if(!grid[i][east].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method takeTiles(List<Pair<Integer, Integer>>)
     * @author Angelo Di Rosa
     * @param positions list of positions of the tiles that the player wants to take
     * @throws NullPointerException if the pair of index is above the limit used for the board (9 x 9)
     * @throws RuntimeException if the pair of index correspond to a 'NOTVALID' cell.
     * @throws RuntimeException if the move is not valid.
     * The method removes the tiles from the board based on the player's move.
     * It puts a 'VALID' value over the selected cell but firts it checks if the move is valid.
     * In order to do so, it calls a new method (emptySides()) that returns a List of index of adjacent cells that are empty
     * (checks how many side of the cell are free from other Tiles).
     * If the List returned (sides) has length = 1, takeTiles checks if that one position correspond to the latest taken tile in the same turn by the same player.
     * If so, the move is not valid and the player needs to make a new different move.
     * If not, the move is valid and the tile is taken (it will be assigned a VALID value to the cell).
     * It also checks if the players is not taking any tiles diagonally by saving the position of the latest taken tile (latestX, latestY).
     * If the latest taken tile has as coordinates (x-1, y-1) with (x,y) being the index of the current tile, it means that the player is trying to move diagonally on the board and this is not allowed in the game.
     * So the method puts again the taken tile on the board  by calling putItBack() and does not allow the player to make the move by restoring the board as if the move never happened.
     * For example: the player wants to take the cells (0,3),(0,4),(1,5). At first takeTiles allowes the player to take the first two tiles and puts a 'VALID' value over the cell.
     * But when takesTiles realizes that the player is trying to move diagonally by taking the (1,5) tile, the method throws a new RuntimeException and calls putItBack() to restore the board
     * as if the move had never taken place (by putting the original values over the previously taken cells (0,3) and (0,4)).*/

    public List<Tiles> takeTiles(List<Pair<Integer, Integer>> positions){
        int x, y, latestX = -2, latestY= -2;
        List<Pair<Integer, Integer>> sides;
        List<Tiles> tilevalues = new ArrayList<>();
        for(int k = 0; k < positions.size(); ++k){
            x = positions.get(k).getFirst();
            y = positions.get(k).getSecond();
            if(x<0 || y>8){
                this.putItBack(positions, tilevalues);
                throw new NullPointerException("These index are not on the board.");
            }
            else{
                tilevalues.add(grid[x][y].getTile());
                if(grid[x][y].isNotValid() || grid[x][y].isEmpty()){
                    this.putItBack(positions, tilevalues);
                    throw new RuntimeException("This cell is not available for the game");
                }
                else if(latestX==x-1 && latestY==y-1){
                    this.putItBack(positions, tilevalues);
                    throw new RuntimeException("Move not valid. Do not choose tiles diagonally.");
                }
                else {
                    sides = this.emptySides(x,y);
                    if (sides.size()==1 && sides.get(0).getFirst()== latestX && sides.get(0).getSecond()== latestY ){
                        this.putItBack(positions, tilevalues);
                        throw new RuntimeException("The move is not valid.");
                    }
                    else if(sides.size()==0){
                        this.putItBack(positions, tilevalues);
                        throw new RuntimeException("Move not valid. NO EMPTY CELLS AROUND");
                    }
                    else{
                        grid[x][y].assignValue(Tiles.VALID);
                        latestX = x;
                        latestY = y;
                    }
                }
            }
        }
        return tilevalues;
    }
    /**Method : emptySide()
     * @param i index 1
     * @param j index 2
     * This method returns a List of Pair of index of the sides that are not adjacent to an object tile */

    public List<Pair<Integer, Integer>> emptySides(int i, int j){
        int n=i-1, s=i+1, e=j+1, w=j-1;
        List<Pair<Integer, Integer>> emptysides = new ArrayList<>();
        if(n < 0 || grid[n][j].isEmpty() || grid[n][j].isNotValid()){
            emptysides.add(Pair.of(n,j));
        }
        if(s > 8|| grid[s][j].isEmpty() || grid[s][j].isNotValid()){
            emptysides.add(Pair.of(s,j));
        }
        if(e > 8 || grid[i][e].isEmpty() || grid[i][e].isNotValid()){
            emptysides.add(Pair.of(i,e));
        }
        if(w < 0 || grid[i][w].isEmpty() || grid[i][w].isNotValid()){
            emptysides.add(Pair.of(i,w));
        }
        return emptysides;
    }

    /**method: putItBack
     * @param moves the player's move
     * @param t a List of tile values of the player's selected cells
     * @author Angelo Di Rosa
     * This method restores the cells by putting their original tile on it whenever a player makes a wrong move.
     */
    public void putItBack(List<Pair<Integer,Integer>> moves, List<Tiles> t){
        for(int i = 0; i < t.size(); ++i){
            int x = moves.get(i).getFirst();
            int y = moves.get(i).getSecond();
            grid[x][y].assignValue(t.get(i));
        }
    }

    public boolean areInLineHorizontally(List<Pair<Integer, Integer>> positions){
        int temp_x = 0, temp_y=0;
        if(positions.size()==1){
            return true;
        }
        int i = 0;
        while(i<positions.size()-1){
            temp_x= positions.get(i+1).getFirst();
            temp_y= positions.get(i+1).getSecond();
            if(temp_x!=positions.get(i).getFirst()){
                return false;
            }
            else{
                if(temp_y != positions.get(i).getSecond()+1){
                    return false;
                }
            }
            ++i;
        }
        return true;
    }

    public boolean areInLineVertically(List<Pair<Integer, Integer>> positions){
        int temp_x = 0, temp_y=0;
        if(positions.size()==1){
            return true;
        }
        int i = 0;
        while(i<positions.size()-1){
            temp_x= positions.get(i+1).getFirst();
            temp_y= positions.get(i+1).getSecond();
            if(temp_y!=positions.get(i).getSecond()){
                return false;
            }
            else{
                if(temp_x != positions.get(i).getFirst()+1){
                    return false;
                }
            }
            ++i;
        }
        return true;
    }

    public String gridToString(){
        var buf = new StringBuffer();
        for(int i = 0 ; i < 9; ++i){
            buf.append(i).append("\t");
            for(int j = 0; j < 9; ++j){
                if (grid[i][j].getTile() == Tiles.NOTVALID)
                    buf.append(" ");
                else if (grid[i][j].getTile() == Tiles.VALID)
                    buf.append("-");
                else
                    buf.append(grid[i][j].getTile().toString().charAt(0));
                buf.append(" ");
            }
            buf.append("\n");
        }
        buf.append("    ");
        for (int i = 0; i < 9; i++)
            buf.append(i).append(" ");
        buf.append("\n");

        return buf.toString();
    }

    public void printBoard() {
        System.out.println(gridToString());
    }

    public Cell[][] getGrid() {
        return grid;
    }
}
