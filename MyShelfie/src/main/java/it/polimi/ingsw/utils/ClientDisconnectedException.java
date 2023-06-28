package it.polimi.ingsw.utils;

/**
 * @author Paolo Ceffa
 * Exception raises when the client is classified as disconnected
 *
 */
public class ClientDisconnectedException extends Exception {
    public ClientDisconnectedException(){
        super("Client disconnected.");
    }
}
