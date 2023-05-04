package it.polimi.ingsw.server.network;

import it.polimi.ingsw.model.logic.Logic;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface: ServerNetworkManager
 * @author Paolo Ceffa
 * Low level network interface of the web server
 */
public interface ServerNetworkManager {
    /**
     * It's the main endpoint used by a client to establish a new connection with the web sever, it also manages the
     * reconnection attempt of a player.
     * @param activePlayers defines the clients currently handled by the web server
     * @param clientHandlers stores the clientHandler of each client currently handled
     * @param activeGames stores all the currently games
     * @return the new accepted clients ready for a new game, by a couple of unique username identifier and a
     * ClientHandler object used to manage the communication with the client
     */
    public ConcurrentHashMap<String, ClientHandler> acceptNewClients(ConcurrentHashMap<String, Integer> activePlayers,
                                                           ConcurrentHashMap<String, ClientHandler> clientHandlers,
                                                                     ConcurrentHashMap<Integer, Logic> activeGames);

    /**
     * Stop and close the network services of the web server
     *
     */
    public void stop();
}
