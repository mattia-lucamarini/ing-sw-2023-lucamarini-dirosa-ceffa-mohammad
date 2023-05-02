package it.polimi.ingsw.client;

import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiInterface;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiService;
import it.polimi.ingsw.network.ClientHandler.SocketClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.server.network.RmiServerServices.RmiServerInterface;
import it.polimi.ingsw.server.network.RmiServerServices.RmiServerService;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Just a random Client Application to perform some test
 * @author Paolo Ceffa
 */
public class TestClient {
    public static void main(String[] args) throws Exception {

        String username = "paul";
        String network = "RMI";

        ClientHandler clientHandler = null;

        if(network.equals("Socket")) {
            Socket socket = new Socket("127.0.0.1", 59090);
            clientHandler = new SocketClientHandler(socket);
        }
        else{
            Registry registry = LocateRegistry.getRegistry();
            RmiServerInterface RmiServer = (RmiServerInterface) registry.lookup("RmiServer");
            RmiInterface rmiClientService = RmiServer.getRmiClientService();
            clientHandler = new RmiClientHandler(rmiClientService);
        }

        clientHandler.receivingKernel();
        clientHandler.pingKernel();
        boolean flag = false;
        try {
            flag = clientHandler.sendingWithRetry(new LoginRequest(username), 2, 1);
        } catch (ClientDisconnectedException e) {
            System.out.println("disconnected from the server before sending log in request.");
            clientHandler.stopConnection();
            return;
        }
        if (!flag) {
            System.out.println("Can't send the log in request.");
            clientHandler.stopConnection();
            return;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        }catch(InterruptedException ignored){
        }
        Message message = null;
        try {
            message = (Message) clientHandler.receivingWithRetry(2, 1);
        } catch (NoMessageToReadException e) {
            System.out.println("No message received after sending the login request");
            clientHandler.stopConnection();
            return;
        } catch (ClientDisconnectedException e) {
            System.out.println("disconnected from the server after sending the login request.");
            clientHandler.stopConnection();
            return;
        }
        if (message.getMessageType().equals(MessageCode.NUM_PLAYERS_REQUEST)) {
            Scanner t = new Scanner(System.in);
            System.out.println("Inserire numero di giocatori: ");
            int num = t.nextInt();
            while(num<1 || num >4){
                System.out.println("The number of players must be between 1-4!");
                System.out.println("Inserire numero di giocatori: ");
                num = t.nextInt();
            }
            try {
                flag = clientHandler.sendingWithRetry(new NumPlayersMessage(num), 2, 1);
            } catch (ClientDisconnectedException e) { //here if I took much time to send the request
                System.out.println("disconnected from the server before sending NumPlayer Message.");
                clientHandler.stopConnection();
                return;
            }
            if (!flag) {
                System.out.println("Can't send the NumPlayer Message.");
                clientHandler.stopConnection();
                return;
            }
            try {
                message = (Message) clientHandler.receivingWithRetry(10, 2);
            } catch (NoMessageToReadException e) {
                System.out.println("No message received after sending the num player message");
                clientHandler.stopConnection();
                return;
            } catch (ClientDisconnectedException e) {
                System.out.println("disconnected from the server while waiting for log response after num player mess.");
                clientHandler.stopConnection();
                return;
            }
        }
        //get log response
        if (message.getMessageType().equals(MessageCode.LOGIN_REPLY)) {
            if (((LoginReply) message).getOutcome()) {
                System.out.println("Client added!!");
            } else {
                System.out.println("Client refused: choose another username.");
                clientHandler.stopConnection();
                return;
            }
        } else {
            System.out.println("Unknown message code received.");
            clientHandler.stopConnection();
            return;
        }
        System.out.println("Waiting for other players...");
        try {
            message = clientHandler.receivingWithRetry(100, 2);
        } catch (NoMessageToReadException e) {
            System.out.println("Not enough players to start e  new  game.");
            clientHandler.stopConnection();
            return;
        } catch (ClientDisconnectedException e) {
            System.out.println("disconnected from the server while waiting for other players.");
            clientHandler.stopConnection();
            return;
        }
        if (message.getMessageType().equals(MessageCode.GENERIC_MESSAGE)) {
            int testIterations = 0;
            boolean status;
            while (testIterations < 10) {
                try {
                    status = clientHandler.sendingWithRetry(new Message(MessageCode.GENERIC_MESSAGE),
                            2, 1);
                    System.out.println("Message sent: " + (testIterations + 1));
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ignored) {
                    }
                } catch (ClientDisconnectedException e) {
                    System.out.println("disconnected from the server while sending message: " + (testIterations + 1));
                    clientHandler.stopConnection();
                    return;
                }
                if (!status) {
                    System.out.println("Error sending the message: " + (testIterations + 1));
                    clientHandler.stopConnection();
                    return;
                }
                testIterations += 1;
            }
            System.out.println("Sent all messages. Test completed !");
        } else {
            System.out.println("Unknown message code received. Test failed");
        }
        clientHandler.stopConnection();
    }
}
