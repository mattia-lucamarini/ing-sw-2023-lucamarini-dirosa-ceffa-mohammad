package it.polimi.ingsw.network.message;

public class FullShelf extends Message {
    private String username;
    private boolean outcome;
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
