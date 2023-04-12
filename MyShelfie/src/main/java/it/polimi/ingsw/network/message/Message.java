package it.polimi.ingsw.network.message;

import it.polimi.ingsw.network.message.MessageCode;

import java.io.Serializable;

/**
 * Generic message class which represents the base entity exchanged during communication between Server and Client
 * @author Paolo Ceffa
 */
public class Message implements Serializable {
    private final String username;
    private final MessageCode messageType;

    /**
     * Default constructor
     *
     * @param username unique username of the client
     * @param messageType identification code of the type of message
     */
    public Message(String username, MessageCode messageType) {
        this.username = username;
        this.messageType = messageType;
    }

    public String getUsername() {
        return username;
    }

    public MessageCode getMessageType() {
        return messageType;
    }

}
