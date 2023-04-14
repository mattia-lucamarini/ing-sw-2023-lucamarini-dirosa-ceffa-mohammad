package it.polimi.ingsw.server;

import it.polimi.ingsw.TestGameLogic;
import it.polimi.ingsw.server.network.ServerNetworkManager;
import it.polimi.ingsw.server.network.ServerRMINetwork;
import it.polimi.ingsw.server.network.ServerSocketNetwork;

import java.io.IOException;
import java.util.HashMap;
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
    private String type;
    private final ServerNetworkManager mainNetworkManager;
    private final ConcurrentHashMap<Integer, TestGameLogic> activeGames;
    private final ConcurrentHashMap<String, Integer> activePlayers;
    private Integer gamesCounter;

    /**
     * Default constructor.
     *
     * @param type specifies the network technology used by the Server between the ones available: [Socket, RMI]
     * @throws IOException if the ServerSocket it's not correctly instantiated
     */
    public WebServer(String type) throws IOException {

        this.type = type;
        this.gamesCounter = 0;
        this.activeGames = new ConcurrentHashMap<>();
        this.activePlayers = new ConcurrentHashMap<>();

        if(this.type.equals("Socket")) {
            this.mainNetworkManager = new ServerSocketNetwork(59090);
        }
        else{
            this.mainNetworkManager = new ServerRMINetwork();
            if(! this.type.equals("RMI")){
                this.type = "RMI";
                LOG.warning("Bad network type. Using RMI by default.");

            }
        }
    }

    /**
     * it's the entrypoint of the entire server-side application, it starts/finalizes/checks the games running different
     * services on different threads.
     *
     */
    public void launchKernel(){
        try {
            new Thread(()-> {
                try {
                    this.checkAndFinalizeGame();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            while (true) {
                HashMap<String, ClientHandler> clients = this.mainNetworkManager.acceptNewClients(this.activePlayers);
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
     * @param clients represents the players of the new game by a unique username and a ClientHandler object
     * used to communicate with the client-side
     *
     */
    private void startNewGame(HashMap<String, ClientHandler> clients){
        TestGameLogic testGameLogic = new TestGameLogic(clients, this.gamesCounter);
        Thread thread = new Thread(testGameLogic);
        thread.start();
        this.activeGames.put(this.gamesCounter, testGameLogic);
        for(String username: clients.keySet()){
            this.activePlayers.put(username, this.gamesCounter);
        }
    }

    /**
     * Method to check the status of a game: if a game is classified as finished, then it remove the related game object
     * and all the players
     *
     * @throws InterruptedException if an error is raised during the waiting time
     */
    private void checkAndFinalizeGame() throws InterruptedException { //fare code refactoring
        while(true){
            System.out.println("Web Server: Active Games: "+this.activeGames.size());
            for(Integer id: this.activeGames.keySet()){
                if(! this.activeGames.get(id).isActive()){
                    this.activeGames.remove(id);
                    System.out.print("Web Server: removing Game "+ id);
                    for(String username: this.activePlayers.keySet()){
                        if(this.activePlayers.get(username).equals(id)){
                            this.activePlayers.remove(username);
                        }
                    }
                }
            }
            TimeUnit.SECONDS.sleep(10);
        }
    }


}
