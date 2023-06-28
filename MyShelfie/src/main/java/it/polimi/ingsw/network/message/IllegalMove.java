package it.polimi.ingsw.network.message;

public class IllegalMove extends Message {
    String reason;
    public IllegalMove(String reason){
        super(MessageCode.MOVE_ILLEGAL);
        this.reason = reason;
    }
    public String getReason() {
        return reason;
    }
}
