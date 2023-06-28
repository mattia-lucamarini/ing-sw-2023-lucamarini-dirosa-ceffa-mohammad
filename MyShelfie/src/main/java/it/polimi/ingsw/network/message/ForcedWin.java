package it.polimi.ingsw.network.message;

/**
 * Used to signal that everyone else disconnected and the last player could become the winner.
 * @author Mattia Lucamarini
 */
public class ForcedWin extends Message {
    private boolean win;

    /**
     * @param win winner status after the timer expires
     */
    public ForcedWin(boolean win){
        super(MessageCode.FORCED_WIN);
        this.win = win;
    }
    public boolean getWin() {
        return win;
    }
}
