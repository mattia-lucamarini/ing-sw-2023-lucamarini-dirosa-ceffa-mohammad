package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Board;

public class PlayTurn extends Message {
    private String username;
    private Board board;
    public PlayTurn(String username){
        super(MessageCode.PLAY_TURN);
        this.username = username;
    }
    public String getUsername(){
        return this.username;
    }
    public Board getBoard() {return this.board;}
}
