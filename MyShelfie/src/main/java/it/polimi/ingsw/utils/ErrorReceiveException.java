package it.polimi.ingsw.utils;

public class ErrorReceiveException extends Exception {
    public ErrorReceiveException(){
        super("Error occurred trying to receive a message.");
    }
}
