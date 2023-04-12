package it.polimi.ingsw.server;

import it.polimi.ingsw.network.message.Message;


/**
 * Abstract class to manage the communication with the client
 *
 */
abstract public class ClientHandler {

    /**
     * Raw method to receive a message from the client
     * @return the received Message object
     * @throws Exception if an exception occurs trying to receive a message
     */
    public abstract Message receive() throws Exception;

    /**
     * Raw method to send a message to the client
     * @param message message to send
     * @return a boolean value about the status of the sending
     */

    public abstract boolean send(Message message);

}
