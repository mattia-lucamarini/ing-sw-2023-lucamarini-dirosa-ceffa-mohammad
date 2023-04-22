package it.polimi.ingsw.utils;

/**
 * Class: NoMessageToReadException
 * @author Paolo Ceffa
 * Custom Exception to raise when no message is found in the queue
 */
public class NoMessageToReadException extends Exception {
    public NoMessageToReadException(){
        super("There are no unread messages in the queue.");
    }
}
