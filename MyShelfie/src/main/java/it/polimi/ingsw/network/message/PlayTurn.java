package it.polimi.ingsw.network.message;

public class PlayTurn extends Message {
    private String username;
    public PlayTurn(String username){
        super(MessageCode.PLAY_TURN);
        this.username = username;
    }
    public String getUsername(){
        return this.username;
    }
}
