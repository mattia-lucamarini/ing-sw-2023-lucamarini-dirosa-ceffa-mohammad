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
        this(username, null);
    }

    public PlayTurn(String username, Board board){
        super(MessageCode.PLAY_TURN);
        this.username = username;
        this.board = board;
    }

    public String getUsername(){
        return this.username;
    }
    public Board getBoard() {return this.board;}

    public void setBoard(Board board) {
        this.board = board;
    }
}
