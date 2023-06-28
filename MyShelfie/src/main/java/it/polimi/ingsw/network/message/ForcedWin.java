package it.polimi.ingsw.network.message;

public class ForcedWin extends Message {
    private boolean win;
    public ForcedWin(boolean win){
        super(MessageCode.FORCED_WIN);
        this.win = win;
    }
    public boolean getWin() {
        return win;
    }
}
