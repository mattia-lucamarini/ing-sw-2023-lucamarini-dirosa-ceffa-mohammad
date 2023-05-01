package it.polimi.ingsw.client;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.ClientHandler.SocketClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Class: Client
 * This class manages all the requests between the players and the game server.
 * @author Mattia Lucamarini
 */
public class Client {
    private static Player player;
    private static PersonalGoalCard goalCard;
    private static Pair<CommonGoal, CommonGoal> commonGoals;
    private static Board board;
    private static boolean gameOn;
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
                System.out.println("Disconnected from the server before sending log in request.");
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
                message = clientHandler.receivingWithRetry(2, 1);
            } catch (NoMessageToReadException e) {
                System.out.println("No message received after sending the login request");
                clientHandler.stopConnection();
                return;
            } catch (ClientDisconnectedException e) {
                System.out.println("Disconnected from the server after sending the login request.");
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
                    message = clientHandler.receivingWithRetry(10, 2);
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
                    System.out.println("\nClient added!");
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

            //PROCESS PERSONAL GOAL
            while (goalCard == null || commonGoals == null) {
                try{
                    //System.out.println("Receiving personal goal..");
                    message = clientHandler.receivingWithRetry(10, 5);
                } catch (NoMessageToReadException e) {
                    System.out.println("No message received after sending the num player message");
                    clientHandler.stopConnection();
                    return;
                } catch (ClientDisconnectedException e) {
                    System.out.println("Disconnected from the server while waiting for log response after num player mess.");
                    clientHandler.stopConnection();
                    return;
                }
                if (message.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL)) {
                    int goalNumber = ((SetPersonalGoal) message).getGoalNumber();
                    System.out.println("Received Personal Goal " + goalNumber);
                    clientHandler.sendingWithRetry(new SetPersonalGoal(), 1, 1);
                    goalCard = new PersonalGoalCard(goalNumber);
                /*for (Map.Entry<Pair<Integer, Integer>, Tiles> i : goalCard.getGoal().getConstraint().entrySet())
                    System.out.println(i);*/
                }
                if (message.getMessageType().equals(MessageCode.SET_COMMON_GOALS)) {
                    int numPlayers = ((SetCommonGoals) message).getNumPlayers();
                    commonGoals = new Pair<>(CommonGoal.all(numPlayers).get(((SetCommonGoals) message).getGoalsIndexes().getFirst()), CommonGoal.all(numPlayers).get(((SetCommonGoals) message).getGoalsIndexes().getFirst()));
                    System.out.println("The Common Goals are: " + ((SetCommonGoals) message).getGoalsIndexes().getFirst() + " and " + ((SetCommonGoals) message).getGoalsIndexes().getSecond());
                }
            }

            //WAITING TO START GAME
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

            if (message.getMessageType() == MessageCode.GAME_START) {
                gameOn = true;
                System.out.println("The game is now starting");

                //TURN PROCESSING
                //pick tiles
                //insert tiles
                //check common goals
                //check shelf completeness
                while (gameOn){
                    try {
                        message = clientHandler.receivingWithRetry(100, 2);
                    } catch (NoMessageToReadException e) {
                        System.out.println("Stopped receiving turns notifications");
                        clientHandler.stopConnection();
                        return;
                    } catch (ClientDisconnectedException e) {
                        System.out.println("Disconnected from the server while waiting for the next turn.");
                        clientHandler.stopConnection();
                        return;
                    }
                    if (message.getMessageType() == MessageCode.PLAY_TURN) {
                        board = ((PlayTurn) message).getBoard();
                        if (((PlayTurn) message).getUsername().equals(player.getUsername())) {
                            System.out.println("\nIt's my turn!");
                            //clientHandler.sendingWithRetry(new CommonGoalReached(1), 50, 10);
                            //System.out.println("Sent goal");
                            ArrayList<Tiles> playerPick = (ArrayList<Tiles>) board.takeTiles(new ArrayList<>(List.of(Pair.of(3,2), Pair.of(4,1))));
                            player.getShelf().insertTiles(new ArrayList<>(List.of(Pair.of(3,2), Pair.of(4,1))), playerPick);

                            //checking goals
                            for (int i = 0; i < 2; i++){
                                if (List.of(commonGoals.getFirst(), commonGoals.getSecond()).get(i).checkGoal(player.getShelf()) == 1){
                                        clientHandler.sendingWithRetry(new CommonGoalReached(i), 50, 10);
                                }
                            }
                        } else
                            System.out.println(((PlayTurn) message).getUsername() + " can now play.");
                    }
                }
            }

        } catch (Exception ignored){}
    }
}
