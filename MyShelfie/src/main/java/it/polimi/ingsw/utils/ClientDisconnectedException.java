package it.polimi.ingsw.utils;

public class ClientDisconnectedException extends Exception {
    public ClientDisconnectedException(){
        super("Client disconnected.");
    }
}
