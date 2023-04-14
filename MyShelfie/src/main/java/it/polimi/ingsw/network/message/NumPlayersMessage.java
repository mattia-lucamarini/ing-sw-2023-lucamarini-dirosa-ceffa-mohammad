package it.polimi.ingsw.network.message;

/**
 * Class: NumPlayersMessage
 * This message represents the response to the request of insertion of the number of players
 * @author Paolo Ceffa
 */
public class NumPlayersMessage extends Message {
    private final Integer numPlayers;

    /**
     * Default constructor
     *
     * @param username unique username of the Client
     * @param numPlayers valid number of players inserted by the Client
     */
    public NumPlayersMessage(String username, Integer numPlayers) {
        super(username, MessageCode.NUM_PLAYERS_RESPONSE);
        this.numPlayers = numPlayers;
    }

    public Integer getNumPlayers() {
        return numPlayers;
    }
}
