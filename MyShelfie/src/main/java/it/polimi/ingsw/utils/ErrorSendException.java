package it.polimi.ingsw.utils;

public class ErrorSendException extends Exception {
    public ErrorSendException(){
        super("Error occurred trying to send a message.");
    }
}

