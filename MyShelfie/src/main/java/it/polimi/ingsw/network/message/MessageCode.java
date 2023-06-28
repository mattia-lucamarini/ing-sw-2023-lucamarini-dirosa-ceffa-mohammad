package it.polimi.ingsw.network.message;

/**
 * This enum contains all the kind of message available for the communication between Server and Client
 * @author Paolo Ceffa
 */
public  enum MessageCode {
    GENERIC_MESSAGE,
    PING_MESSAGE,
    LOGIN_REQUEST,
    LOGIN_REPLY,
    RECONNECT,
    NUM_PLAYERS_REQUEST,
    NUM_PLAYERS_RESPONSE,
    SET_PERSONAL_GOAL,
    SET_COMMON_GOALS,
    GAME_START,
    PLAYER_ORDER,
    PLAY_TURN,
    COMMON_GOAL_REACHED,
    FULL_SHELF,
    TURN_OVER,
    SHELF_CHECK,
    END_GAME,
    CHOSEN_TILES,
    INSERT,
    MOVE_LEGAL,
    MOVE_ILLEGAL,
    FINAL_SCORE,
    FORCED_WIN,
    BAD_RESPONSE
}
