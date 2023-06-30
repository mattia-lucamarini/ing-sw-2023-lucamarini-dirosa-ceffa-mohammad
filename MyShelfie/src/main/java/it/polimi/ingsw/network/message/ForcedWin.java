package it.polimi.ingsw.network.message;

/**
 * The ForcedWin message is used by the server to signal to the player if he is the last one connected.
 * The server always sends one after the PLAY_TURN message, with its attribute set to true if the player
 * is the last one, false otherwise.
 * In the first case the server starts a 15 seconds timer after which it verifies if anyone reconnected.
 * If that's true, the server sends to the player a ForcedWin message set to true, false otherwise.
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
