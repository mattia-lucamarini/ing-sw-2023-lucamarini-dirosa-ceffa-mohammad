package it.polimi.ingsw.server.network;

import it.polimi.ingsw.server.ClientHandler;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRMINetwork implements ServerNetworkManager {

    @Override
    public HashMap<String, ClientHandler> acceptNewClients(ConcurrentHashMap<String, Integer> activePlayers) {
        return null;
    }

    @Override
    public void stop() {

    }
}
