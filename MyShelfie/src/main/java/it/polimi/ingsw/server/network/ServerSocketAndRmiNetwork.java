package it.polimi.ingsw.server.network;

import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiService;
import it.polimi.ingsw.network.ClientHandler.SocketClientHandler;
import it.polimi.ingsw.network.message.*;


import it.polimi.ingsw.server.WebServer;
import it.polimi.ingsw.server.network.RmiServerServices.RmiServerService;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Class: ServerSocketNetwork
 * @author Paolo Ceffa
 * Implementation of ServerNetworkManager using Socket network technology
 */
public class ServerSocketAndRmiNetwork implements ServerNetworkManager {
    private final int port;
    private final ServerSocket listener;
    private final RmiServerService accepter;
    private final ConcurrentLinkedQueue<ClientHandler> waitingList;


    /**
     * Default constructor
     *
     * @param port specifies the port of the ServerSocket
     * @throws IOException if the ServerSocket it's not correctly instantiated
     */
    public ServerSocketAndRmiNetwork(int port) throws IOException {
        this.port = port;
        this.listener = new ServerSocket(this.port);
        this.accepter = new RmiServerService();
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("RmiServer", this.accepter);
        this.waitingList = new ConcurrentLinkedQueue<>();

        // to do -> manage RMI clients
    }

    /**
     * Implementation of acceptNewClients method of ServerNetworkManager
     *
     * @param activePlayers defines the clients currently handled by the web server
     * @return the new accepted clients ready for a new game, by a couple of unique username identifier and a
     * SocketClientHandler object used to manage the communication with the client through a Socket
     */
    @Override //to implement -> manage the disconnection of an already accepted client while waiting for the others
    public ConcurrentHashMap<String, ClientHandler> acceptNewClients(ConcurrentHashMap<String, Integer> activePlayers,
                                                           ConcurrentHashMap<String, ClientHandler> clientHandlers) {
            ConcurrentHashMap<String, ClientHandler> clientList = new ConcurrentHashMap<>();
            int maxNumPlayers = WebServer.MAX_PLAYERS;
            while (clientList.size() < maxNumPlayers) {
                System.out.println("[Web Server] Wait for a new player.");
                ClientHandler clientHandler =  null;
                while(clientHandler == null){
                    clientHandler = this.waitingList.poll();
                    this.removeDisconnectedWaitingClients(clientList);
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    }catch(InterruptedException ignored){
                    }
                }
                try {
                    LoginRequest message = null;
                    try{
                        message = (LoginRequest) clientHandler.receivingWithRetry(2, 1);
                    }catch(NoMessageToReadException e){
                        throw new ClientDisconnectedException();
                    }
                    String clientUsername = message.getUsername();

                    if(clientList.containsKey(clientUsername)){
                        boolean status = clientHandler.sendingWithRetry(new LoginReply(false),
                                3, 1);
                        if(! status){
                            throw new ClientDisconnectedException();
                        }
                        continue;
                    }
                    if(activePlayers.containsKey(clientUsername)){ //if the client is already in our system
                        if(! clientHandlers.get(clientUsername).isConnected()){
                            // -> recover procedure: we reconnect to player to the game
                        }
                        else{ // this client is already connected and alive -> reject log in
                            boolean status = clientHandler.sendingWithRetry(new LoginReply(false),
                                    3, 1);
                            if(! status){
                                throw new ClientDisconnectedException();
                            }
                        }
                    }
                    else{ // new player
                        if(clientList.size() == 0){ // first player of a new game
                            System.out.println("[Web Server] Sending num player request.");
                            maxNumPlayers = askForNumPlayers(clientUsername, clientHandler);
                        }
                        boolean status = clientHandler.sendingWithRetry(new LoginReply(true),
                                3,2);
                        if(!status) throw new ClientDisconnectedException();
                        clientList.put(clientUsername, clientHandler);
                        System.out.println("[Web Server] new client logged in!");
                    }

                } catch (ClientDisconnectedException e) {
                    WebServer.LOG.warning(("[Web Server] Error adding a new client: " + e.getMessage()));
                    System.out.println("[Web Server] disconnection during log in.");
                } catch (Exception e) {
                    WebServer.LOG.severe(("[Web Server] Unknown Error occurred while adding a new client: " + e.getMessage()));
                    System.out.println("[Web Server] Unknown Error during log in.");
                }

            }// end while

        return clientList;
    }

    /**
     * Method to manage the request of the number of players of the new game with the first player
     *
     * @param username unique identifier of the client
     * @param clientHandler clientHandler object to communicate with him
     * @return the number of player inserted by the client
     * @throws ClientDisconnectedException if the client disconnected, or if he takes too much time to response
     */
    private int askForNumPlayers(String username, ClientHandler clientHandler) throws ClientDisconnectedException {
        boolean status = clientHandler.sendingWithRetry(new Message(MessageCode.NUM_PLAYERS_REQUEST),
                3,2);
        if(!status ) throw new ClientDisconnectedException();
        NumPlayersMessage message = null;
        try {
            message = (NumPlayersMessage) clientHandler.receivingWithRetry(30, 2);
        }catch(NoMessageToReadException e){
            throw new ClientDisconnectedException();  //if the client doesn't give us the number of players in a
            //reasonable time, we close the connection with him to avoid to create a queue of waiting clients
        }
        return message.getNumPlayers();
    }

    /**
     * Method to check and remove disconnected clients waiting for a new game
     *
     * @param clientList represents the list of clients already logged in, waiting for other players.
     */
    private void removeDisconnectedWaitingClients(ConcurrentHashMap<String, ClientHandler> clientList){
        for(String client: clientList.keySet()){
            if(! clientList.get(client).isConnected()){
                clientList.remove(client);
            }
        }
    }

    /**
     * Kernel to establish the connection with new rmi clients
     *
     */
    public void establishRmiConnectionKernel(){
        new Thread(()-> {
            while (true) {
                if (this.accepter.isThereAClient()) {
                    System.out.println("[Web Server] got a RMI client!");
                    ClientHandler clientHandler = new RmiClientHandler((RmiService) this.accepter.getServerService());
                    clientHandler.receivingKernel();
                    clientHandler.pingKernel();
                    this.waitingList.add(clientHandler);
                } else {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }).start();
    }

    /**
     * Kernel to establish the connection with new socket clients
     *
     */
    public void establishSocketConnectionKernel(){
        new Thread(()-> {
            while(true){
                try {
                    Socket socket = listener.accept();
                    System.out.println("[Web Server] got a Socket client!");
                    SocketClientHandler clientHandler = new SocketClientHandler(socket);
                    clientHandler.receivingKernel();
                    clientHandler.pingKernel();
                    this.waitingList.add(clientHandler);
                }catch(IOException e){
                    WebServer.LOG.severe(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Implementation of the stop method of ServerNetworkManager
     *
     */
    @Override
    public void stop() {
        try {
            listener.close();
        } catch (IOException e) {
            WebServer.LOG.warning("Error closing server socket.");
        }
    }
}
