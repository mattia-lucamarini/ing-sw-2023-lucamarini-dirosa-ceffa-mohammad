package it.polimi.ingsw.network.message;

/**
 * Class: PingMessage
 * This message represents just a ping which to establish if the connection client-server is still alive
 * @author Paolo Ceffa
 *
 */

public class PingMessage extends Message {

    public PingMessage() {
        super(MessageCode.PING_MESSAGE);
    }
}

