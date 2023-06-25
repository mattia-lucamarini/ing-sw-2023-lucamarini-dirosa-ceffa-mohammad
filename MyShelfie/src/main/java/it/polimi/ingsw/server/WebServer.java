package it.polimi.ingsw.server;

import it.polimi.ingsw.model.logic.GameLogic;
import it.polimi.ingsw.model.logic.Logic;
import it.polimi.ingsw.model.logic.TestGameLogic;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.server.network.ServerNetworkManager;
import it.polimi.ingsw.server.network.ServerSocketAndRmiNetwork;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Class: WebServer
 * @author Paolo Ceffa
 * WebServer represents the low level services infrastracture which manages the creation and the deletion of a game and
 * its players.
 */
public class WebServer {

    public final static int MAX_PLAYERS = 4; //spostare nella classe GameLogic
    public static final Logger LOG = Logger.getLogger(WebServer.class.getName());
    private final ServerSocketAndRmiNetwork mainNetworkManager;
    private final ConcurrentHashMap<Integer, Logic> activeGames;
    private final ConcurrentHashMap<String, Integer> activePlayers;
    private final ConcurrentHashMap<String, ClientHandler> clientHandlers;
    private Integer gamesCounter;
    private final String executionMode; //it defines the execution of the  web server: test or production

    /**
     * Default constructor.
     *
     * @param executionMode defines the execution mode of the server, test or production, running the
     * consequent GameLogic.
     * @param ports defines the set of ports used by the network managers.
     * @throws IOException if the ServerSocketAndRmiNetwork is not correctly instantiated.
     */
    public WebServer(String executionMode, int[] ports) throws IOException {

        this.gamesCounter = 0;
        this.activeGames = new ConcurrentHashMap<>();
        this.activePlayers = new ConcurrentHashMap<>();
        this.clientHandlers = new ConcurrentHashMap<>();
        this.mainNetworkManager = new ServerSocketAndRmiNetwork(ports[0], ports[1]);
        this.mainNetworkManager.establishSocketConnectionKernel();
        this.mainNetworkManager.establishRmiConnectionKernel();
        if (!executionMode.equals("test") && !executionMode.equals("production")){
            System.out.println("[Web Server] bad selected mode. Using test by default.");
            this.executionMode = "test";
        }
        else {
            this.executionMode = executionMode;
        }
    }

    /**
     * it's the entrypoint of the entire server-side application, it starts/finalizes/checks the games running different
     * services on different threads.
     *
     */
    public void launchKernel(){
        try {
            new Thread(this::checkAndFinalizeGame).start();
            new Thread(this::checkClientConnection).start();
            while (true) {
                ConcurrentHashMap<String, ClientHandler> clients = this.mainNetworkManager.acceptNewClients(
                        this.activePlayers, this.clientHandlers, this.activeGames);
                if (clients != null) {
                    startNewGame(clients);
                    this.gamesCounter += 1;
                }
            }
        }
        finally {
            mainNetworkManager.stop();
        }

    }

    /**
     * It starts a new game on a new thread, and it adds the new clients to the activatePlayers structure.
     *
     * @param clients represents the players of the new game by a unique username and a ClientHandler object
     * used to communicate with the client-side
     *
     */
    private void startNewGame(ConcurrentHashMap<String, ClientHandler> clients){
        Logic gameLogic;
        if(this.executionMode.equals("test")) {
            gameLogic = new TestGameLogic(clients, this.gamesCounter);
        }
        else{
            gameLogic = new GameLogic(clients, this.gamesCounter);
        }
        Thread thread = new Thread((Runnable) gameLogic);
        thread.start();
        this.activeGames.put(this.gamesCounter, gameLogic);
        for(String username: clients.keySet()){
            this.activePlayers.put(username, this.gamesCounter);
            this.clientHandlers.put(username, clients.get(username));
        }
    }

    /**
     * Method to check the status of a game: if a game is classified as finished, then it remove the related game object
     * and all the players
     *
     */
    private void checkAndFinalizeGame() { //fare code refactoring
        while(true){
            System.out.println("[Web Server] Number of active Games: "+this.activeGames.size());
            for(Integer id: this.activeGames.keySet()){
                if(! this.activeGames.get(id).isActive()){
                    this.activeGames.remove(id);
                    System.out.println("[Web Server] removing Game "+ id);
                    for(String username: this.activePlayers.keySet()){
                        if(this.activePlayers.get(username).equals(id)){
                            this.activePlayers.remove(username);
                            this.clientHandlers.remove(username);
                        }
                    }
                }
            }
            try {
                TimeUnit.SECONDS.sleep(10);
            }catch (InterruptedException e){
                WebServer.LOG.warning(e.getMessage());
            }
        }
    }

    /**
     * Method to check the connection status of all the clients in the system
     *
     */
    private void checkClientConnection() {
        while(true){
            for(String username: this.clientHandlers.keySet()) {
                System.out.println("[Web Server] " + username + ": is connected: "
                        + this.clientHandlers.get(username).isConnected());
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            }catch(InterruptedException e){
                WebServer.LOG.warning(e.getMessage());
            }
        }
    }

}
