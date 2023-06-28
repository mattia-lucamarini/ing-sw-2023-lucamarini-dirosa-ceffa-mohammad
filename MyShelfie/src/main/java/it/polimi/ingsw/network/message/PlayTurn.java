package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Board;

/**
 * Signals the turn start. Also carries the updated board.
 * @author Mattia Lucamarini
 */
public class PlayTurn extends Message {
    private String username;
    private Board board;

    /**
     * @param username who's starting the turn
     */
    public PlayTurn(String username){
        super(MessageCode.PLAY_TURN);
        this.username = username;
    }
    public String getUsername(){
        return this.username;
    }
    public Board getBoard() {return this.board;}

    public void setBoard(Board board) {
        this.board = board;
    }
}
