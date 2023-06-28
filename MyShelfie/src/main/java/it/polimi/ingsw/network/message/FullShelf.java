package it.polimi.ingsw.network.message;

/**
 * Used to notify if a player filled their shelf, as determined by the boolean attribute it carries.
 * @author Mattia Lucamarini
 */
public class FullShelf extends Message {
    private String username;
    private boolean outcome;

    /**
     * @param player username
     * @param outcome shelf fullness
     */
    public FullShelf(String player, boolean outcome){
        super(MessageCode.FULL_SHELF);
        this.username = player;
        this.outcome = outcome;
    }

    public String getPlayer() {
        return username;
    }

    public boolean getOutcome() {
        return outcome;
    }
}
