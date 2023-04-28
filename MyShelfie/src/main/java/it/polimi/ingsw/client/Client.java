package it.polimi.ingsw.client;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.ClientHandler.SocketClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Class: Client
 * This class manages all the requests between the players and the game server.
 * @author Mattia Lucamarini
 */
public class Client {
    public static Player player;
    public static PersonalGoalCard goalCard;
    public static void main(String[] args) {
        System.out.print("Inserisci il tuo username: ");
        Scanner sc = new Scanner(System.in);
        player = new Player(sc.nextLine());

        try (Socket socket = new Socket("127.0.0.1", 59090)) {
            ClientHandler clientHandler = new SocketClientHandler(socket);
            clientHandler.receivingKernel();
            clientHandler.pingKernel();

            //LOGIN REQUEST
            boolean flag = false;
            try {
                flag = clientHandler.sendingWithRetry(new LoginRequest(player.getUsername()), 1, 1);
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

            //RECEIVING NUMREQUEST
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
                System.out.print("Inserire numero di giocatori: ");
                int num = t.nextInt();
                while(num<1 || num >4){
                    System.out.println("The number of players must be between 1-4!");
                    System.out.print("Inserire numero di giocatori: ");
                    num = t.nextInt();
                }
                try {
                    flag = clientHandler.sendingWithRetry(new NumPlayersMessage(num), 2, 1);
                } catch (ClientDisconnectedException e) { //here if I took much time to send the request
                    System.out.println("Disconnected from the server before sending NumPlayer Message.");
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
                    System.out.println("Disconnected from the server while waiting for log response after num player mess.");
                    clientHandler.stopConnection();
                    return;
                }
            }

            //RECEIVING LOGIN REPLY
            if (message.getMessageType().equals(MessageCode.LOGIN_REPLY)) {
                if (((LoginReply) message).getOutcome()) {
                    System.out.println("Client added!!");
                } else {
                    System.out.println("Client refused: choose another username.");
                    clientHandler.stopConnection();
                    return;
                }
                try{
                    System.out.println("Receiving personal goal..");
                    message = clientHandler.receivingWithRetry(100, 5);
                } catch (NoMessageToReadException e) {
                    System.out.println("No message received after sending the num player message");
                    clientHandler.stopConnection();
                    return;
                } catch (ClientDisconnectedException e) {
                    System.out.println("Disconnected from the server while waiting for log response after num player mess.");
                    clientHandler.stopConnection();
                    return;
                }
            } else {
                System.out.println("Unknown message code received.");
                clientHandler.stopConnection();
                return;
            }

            //PROCESS PERSONAL GOAL
            if (message.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL)){
                int goalNumber = ((SetPersonalGoal) message).getGoalNumber();
                System.out.println("Received goal "+ goalNumber);
                clientHandler.sendingWithRetry(new SetPersonalGoal(), 1, 1);
                goalCard = new PersonalGoalCard(goalNumber);
                /*for (Map.Entry<Pair<Integer, Integer>, Tiles> i : goalCard.getGoal().getConstraint().entrySet())
                    System.out.println(i);*/
            }

            System.out.println("Waiting for other players...");
            try {
                message = clientHandler.receivingWithRetry(100, 2);
            } catch (NoMessageToReadException e) {
                System.out.println("Not enough players to start a new game.");
                clientHandler.stopConnection();
                return;
            } catch (ClientDisconnectedException e) {
                System.out.println("Disconnected from the server while waiting for other players.");
                clientHandler.stopConnection();
                return;
            }
        } catch (Exception ignored){}
    }
}
