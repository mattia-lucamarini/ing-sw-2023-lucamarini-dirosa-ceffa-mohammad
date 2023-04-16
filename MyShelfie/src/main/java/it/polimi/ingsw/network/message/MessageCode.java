package it.polimi.ingsw.network.message;

/**
 * This enum contains all the kind of message available for the communication between Server and Client
 * @author Paolo Ceffa
 */
public  enum MessageCode {
    GENERIC_MESSAGE,
    LOGIN_REQUEST,
    LOGIN_REPLY,
    NUM_PLAYERS_REQUEST,
    NUM_PLAYERS_RESPONSE,
    SET_PERSONAL_GOAL
}
