package it.polimi.ingsw.network.message;

import it.polimi.ingsw.network.message.MessageCode;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Generic message class which represents the base entity exchanged during communication between Server and Client
 * @author Paolo Ceffa
 */
public class Message implements Serializable {
    private final MessageCode messageType;

    /**
     * Default constructor
     * @param messageType identification code of the type of message
     */
    public Message(MessageCode messageType){
        this.messageType = messageType;
    }

    public MessageCode getMessageType() {
        return messageType;
    }

}
