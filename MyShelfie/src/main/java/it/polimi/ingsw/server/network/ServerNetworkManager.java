package it.polimi.ingsw.server.network;

import it.polimi.ingsw.server.ClientHandler;

import java.util.HashMap;
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
     * @return the new accepted clients ready for a new game, by a couple of unique username identifier and a
     * ClientHandler object used to manage the communication with the client
     */
    public HashMap<String, ClientHandler> acceptNewClients(ConcurrentHashMap<String, Integer> activePlayers);

    /**
     * Stop and close the network services of the web server
     *
     */
    public void stop();
}
