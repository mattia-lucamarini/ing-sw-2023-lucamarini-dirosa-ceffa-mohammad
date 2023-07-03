package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Board;

public class GameStart extends Message {
    private Board board;
    public GameStart(Board board){
        super(MessageCode.GAME_START);
        this.board = board;
    }
    public GameStart() {
        super(MessageCode.GAME_START);
    }
    public Board getBoard() {
        return board;
    }
}
