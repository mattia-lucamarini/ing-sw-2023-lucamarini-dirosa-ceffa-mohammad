package it.polimi.ingsw;
import java.util.Random;
/**
 * Class: Board
 * @author Angelo Di Rosa
 * This class is an abstraction og the physical board where the game takes place.
 * It consists in a 9x9 matrix of Cells that can available (or not) for the game.
 * The contructor, in fact, inizialize the single Cells by setting a VALID/NOTVALID value depending on if they're used for the game.
 * The constructor inizialize the grid as if it was used for a two player only game.
 * If the game is played by 3 or more players, it is be "prepareBoard" 's job to modify the Board.
 */
public class Board {
    private static Board instance;
    private Cell[][] grid;
    Bag bag= new Bag();
    private Board(){
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

    }
    /**
     * Method: prepareBoard
     * @param numplayers numbers of players
     * This method adapts the Board for a 3 more players game. It puts the value "VALID" on some of those Cells that weren't initially available for a 2 player game.
     * */
    public Board prepareBoard(int numplayers){
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
        return instance ;
    }
    /**Method: getBoard
     * @author Angelo Di Rosa
     * this is a getter method. It returns the Board or instantiates a new one. */

    public Board getBoard() {
        if (instance == null){
            instance = new Board();
        }
        return instance;
    }
    /**
     * Method : refillBoard
     * @author Angelo Di Rosa
     * refillBoard is used to fill the board with the tiles to start/keep playing the game.
     * It iterates on the board asking if the single cell has a valid/notvalid value.
     * If the cell has a "NOTVALID" value, refillBoard won't replace it with another tile (as a matter of fact that cell can't be used for the game).
     * If the cell has a "VALID" value (which means the cell can be used for the game), refillBoard will replace it with another tile.
     * This method calls val.nextInt(6) which generates a random number from 0 to 5. This number is stored and used as a parameter for bag.remainingTiles().
     * remainingTiles counts how many tiles of the same type are left. */

    public void refillBoard(){
        Random val = new Random();
        Tiles t = Tiles.NOTVALID;
        for(int i = 0; i < 9 ; ++i){
            for(int j = 0 ; j < 9; ++j){
                if(grid[i][j].isValid()== true){
                    while(t == Tiles.NOTVALID){
                        int r = val.nextInt(6);
                        t = bag.remainingTiles(r);
                    }
                    grid[i][j].assignValue(t);
                }
            }
        }
    }


}
