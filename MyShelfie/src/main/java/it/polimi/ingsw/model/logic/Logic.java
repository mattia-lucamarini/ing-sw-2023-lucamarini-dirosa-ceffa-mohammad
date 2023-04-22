package it.polimi.ingsw.model.logic;

/** Logic Interface
 * to implement to develop a new game logic to instantiate on the web server
 *
 */

public interface Logic {

    /**
     * Method to check if the implemented game is terminated
     * @return the status of the game
     *
     */
    public boolean isActive();

    }
