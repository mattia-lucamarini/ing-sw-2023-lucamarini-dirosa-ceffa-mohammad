package it.polimi.ingsw.server.network;

import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.server.ClientHandler;
import it.polimi.ingsw.server.SocketClientHandler;
import it.polimi.ingsw.server.WebServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Class: ServerSocketNetwork
 * @author Paolo Ceffa
 * Implementation of ServerNetworkManager using Socket network technology
 */
public class ServerSocketNetwork implements ServerNetworkManager {
    private final int port;
    private final ServerSocket listener;

    /**
     * Default constructor
     * @param port specifies the port of the ServerSocket
     * @throws IOException if the ServerSocket it's not correctly instantiated
     */
    public ServerSocketNetwork(int port) throws IOException {
        this.port = port;
        this.listener = new ServerSocket(this.port);
    }

    /**
     * Implementation of acceptNewClients method of ServerNetworkManager
     *
     * @param activePlayers defines the clients currently handled by the web server
     * @return the new accepted clients ready for a new game, by a couple of unique username identifier and a
     * SocketClientHandler object used to manage the communication with the client through a Socket
     */
    @Override
    public HashMap<String, ClientHandler> acceptNewClients(ConcurrentHashMap<String, Integer> activePlayers) {
        HashMap<String, ClientHandler> clientList = new HashMap<>();
        try {
            int numConnectedPlayers = 0;
            int maxNumPlayers = WebServer.MAX_PLAYERS;
            while (numConnectedPlayers < maxNumPlayers) {
                System.out.println("Web Server: Wait for a new player.");
                try {
                    Socket socket = listener.accept();
                    TimeUnit.SECONDS.sleep(2);

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    LoginRequest message = (LoginRequest) in.readObject();
                    String clientUsername = message.getUsername(); // check client side not null username

                    if (activePlayers.containsKey(clientUsername)) {
                        System.out.println("User: " + clientUsername + " is trying to reconnect to an existing game.");
                        //  To implement -> recover procedure to verify if it is the request of a disconnect user
                        // and in this case we try to reconnect him, otherwise we reject the request with a
                        // username already exists response (default protocol)
                        out.writeObject(new LoginReply(clientUsername, false));
                        out.flush();
                    } else if (!clientList.containsKey(clientUsername)) {
                        if (numConnectedPlayers == 0) {
                            try { // to do -> manage errors
                                maxNumPlayers = askForNumPlayers(out, in, clientUsername);
                            } catch (Exception e) {
                                WebServer.LOG.severe("Error asking for num of players.");
                            }
                        }
                        out.writeObject(new LoginReply(clientUsername, true));
                        out.flush();
                        clientList.put(clientUsername, new SocketClientHandler(in ,out));
                        System.out.println("Web Server: got one new client!");
                        numConnectedPlayers += 1;

                    } else {
                        out.writeObject(new LoginReply(clientUsername, false));
                        out.flush();
                    }

                } catch (IOException e) {
                    WebServer.LOG.warning(("Error adding a new client: " + e.toString()));
                } catch (InterruptedException e) {
                    WebServer.LOG.warning(("Error waiting for a new client: " + e.toString()));
                }

            }
        } catch (Exception e) {
            WebServer.LOG.severe("Unknown error in acceptNewClients: " + e.toString());
            return null;
        }
        return clientList;
    }

    /**
     * Manage the request of the number of players for a new game
     *
     * @param out defines the ObjectOutputStream of the client
     * @param in defines the ObjectInputStream of the client
     * @param username unique username of the client
     * @return the valid number of players selected by the client
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private int askForNumPlayers(ObjectOutputStream out, ObjectInputStream in, String username) throws IOException,
            ClassNotFoundException {
        out.writeObject(new Message(username, MessageCode.NUM_PLAYERS_REQUEST));
        NumPlayersMessage message = (NumPlayersMessage) in.readObject();
        return message.getNumPlayers();
    }

    /**
     * Implementation of the stop method of ServerNetworkManager
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
