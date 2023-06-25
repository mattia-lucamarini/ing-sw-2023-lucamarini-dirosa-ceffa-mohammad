package it.polimi.ingsw.model.logic;

import it.polimi.ingsw.network.ClientHandler.ClientHandler;

/** Logic Interface
 * @author Paolo Ceffa
 * to implement to develop a new game logic to instantiate on the web server
 *
 */

public interface Logic {

    /**
     * Method to check if the implemented game is terminated
     * @return the status of the game
     *
     */
    boolean isActive();

    /**
     * Method to reconnect a player to the game. It used by the webserver during the login phase
     *
     * @param username the unique identifier of a player
     * @param clientHandler the object to manage the communication. It should be updated in the Game instance
     * @return the status of the reconnection
     */
    boolean reconnectPlayer(String username, ClientHandler clientHandler, String gameStatus);

    }


