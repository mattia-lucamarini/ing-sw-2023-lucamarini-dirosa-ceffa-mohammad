package it.polimi.ingsw.client;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiInterface;
import it.polimi.ingsw.network.ClientHandler.SocketClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.server.network.RmiServerServices.RmiServerInterface;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;
import it.polimi.ingsw.view.CLIInterface;
import it.polimi.ingsw.view.UserInterface;
import it.polimi.ingsw.view.View;

import java.io.IOException;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Class: Client
 * This class manages all the requests between the players and the game server.
 * @author Mattia Lucamarini
 */
public class Client {
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    public static Player player;
    public static ClientHandler clientHandler;
    private static UserInterface userInterface;
    public static PersonalGoalCard goalCard;
    private static Pair<CommonGoal, CommonGoal> commonGoals;
    private static ArrayList<CommonGoal> goalList;
    public static ArrayList<String> playerOrder;
    public static HashMap<String, Shelf> playerShelves;
    public static Board board;
    private static boolean gameOn;

    public static void main(String[] args) {
        //SELECT INTERFACE AND NETWORK TYPE
        System.out.println("Welcome to My Shelfie!\n Type 1 if you'd like to play by using a TUI\n Type 2 if you'd like to play by using a GUI ");
        System.out.print("> ");
        Scanner sc = new Scanner(System.in);
        Socket socket;
        Registry registry;
        RmiServerInterface RmiServer;
        RmiInterface rmiClientService;

        switch (sc.nextLine()){
            case "1":
                userInterface = new CLIInterface();
                System.out.println(" Type 1 if you'd like to play using sockets\n Type 2 if you'd like to play using RMI");
                System.out.print("> ");
                try {
                    switch (sc.nextLine()) {
                        case "1":
                            socket = new Socket("127.0.0.1", 59090);
                            clientHandler = new SocketClientHandler(socket);
                            break;
                        case "2":
                            registry = LocateRegistry.getRegistry();
                            RmiServer = (RmiServerInterface) registry.lookup("RmiServer");
                            rmiClientService = RmiServer.getRmiClientService();
                            clientHandler = new RmiClientHandler(rmiClientService);
                            break;
                        default:
                            System.out.println("Invalid option. Defaulting to sockets");
                            socket = new Socket("127.0.0.1", 59090);
                            clientHandler = new SocketClientHandler(socket);
                            break;
                    }
                player = userInterface.askForUsername();
                } catch (Exception e){
                    System.out.println(e);
                }
                break;
            case "2":
                View view = new View();
                view.main(args);
                break;
        }

        //CONNECT TO SERVER AND START GAME PROCESSING
        try {
            clientHandler.receivingKernel();
            clientHandler.pingKernel();

            while (!login())
                ;
            while (!goalProcessing())
                ;
            waitForOrder();

            Message message = new Message(MessageCode.GENERIC_MESSAGE);
            try {
                message = clientHandler.receivingWithRetry(100, 2);
            } catch (NoMessageToReadException e) {
                userInterface.printErrorMessage("Not enough players to start a new game.");
            } catch (ClientDisconnectedException e) {
                userInterface.printErrorMessage("Disconnected from the server while waiting for other players.");
            }

            if (message.getMessageType() == MessageCode.GAME_START) {
                gameOn = true;
                userInterface.showGameStart();

                //TURN PROCESSING
                while (gameOn){
                    playTurn();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static boolean login() {
        //LOGIN REQUEST
        boolean flag = false;
        try {
            flag = clientHandler.sendingWithRetry(new LoginRequest(player.getUsername()), ATTEMPTS, WAITING_TIME);
        } catch (ClientDisconnectedException e) {
            userInterface.printErrorMessage("Disconnected from the server before sending log in request.");
            return false;
        }
        if (!flag) {
            userInterface.printErrorMessage("Can't send the log in request.");
            return false;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException ignored) {
        }

        //RECEIVING NUMREQUEST
        Message message = null;
        try {
            message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
        } catch (NoMessageToReadException e) {
            userInterface.printErrorMessage("No message received after sending the login request");
            return false;
        } catch (ClientDisconnectedException e) {
            userInterface.printErrorMessage("Disconnected from the server after sending the login request.");
            return false;
        }

        if (message.getMessageType().equals(MessageCode.NUM_PLAYERS_REQUEST)) {
            try {
                userInterface.askForNumOfPlayers(clientHandler);
            } catch (IOException e) {
                throw new RuntimeException("To implement");
            }

            try {
                message = clientHandler.receivingWithRetry(10, 2);
            } catch (NoMessageToReadException e) {
                userInterface.printErrorMessage("No message received after sending the num player message");
                return false;
            } catch (ClientDisconnectedException e) {
                userInterface.printErrorMessage("Disconnected from the server while waiting for log response after num player mess.");
                return false;
            }
        }

        //RECEIVING LOGIN REPLY
        if (message.getMessageType().equals(MessageCode.LOGIN_REPLY)) {
            if (((LoginReply) message).getOutcome()) {
                userInterface.printMessage("\nClient added!");
                return true;
            } else {
                userInterface.printErrorMessage("Client refused: choose another username.");
                return false;
            }
        } else {
            userInterface.printErrorMessage("Unknown message code received. ("+message.getMessageType()+")");
            return false;
        }
    }
    private static boolean goalProcessing() {
        //PROCESS PERSONAL GOAL
        Message message;
        while (goalCard == null || commonGoals == null) {
            try {
                //System.out.println("Receiving personal goal..");
                message = clientHandler.receivingWithRetry(10, 5);
            } catch (NoMessageToReadException e) {
                userInterface.printErrorMessage("No message received after sending the num player message");
                return false;
            } catch (ClientDisconnectedException e) {
                userInterface.printErrorMessage("Disconnected from the server while waiting for log response after num player mess.");
                return false;
            }
            if (message.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL)) {
                int goalNumber = ((SetPersonalGoal) message).getGoalNumber();
                userInterface.showPersonalGoal(goalNumber);
                try {
                    clientHandler.sendingWithRetry(new SetPersonalGoal(), 1, 1);
                } catch (Exception e) {
                    userInterface.printErrorMessage(e.toString());
                    return false;
                }
                goalCard = new PersonalGoalCard(goalNumber);
                /*for (Map.Entry<Pair<Integer, Integer>, Tiles> i : goalCard.getGoal().getConstraint().entrySet())
                    System.out.println(i);*/
            }
            if (message.getMessageType().equals(MessageCode.SET_COMMON_GOALS)) {
                int numPlayers = ((SetCommonGoals) message).getNumPlayers();
                commonGoals = new Pair<>(CommonGoal.all(numPlayers).get(((SetCommonGoals) message).getGoalsIndexes().getFirst()), CommonGoal.all(numPlayers).get(((SetCommonGoals) message).getGoalsIndexes().getFirst()));
                userInterface.showCommonGoals(((SetCommonGoals) message).getGoalsIndexes().getFirst(), ((SetCommonGoals) message).getGoalsIndexes().getSecond());
            }
        }
        return true;
    }
    private static void waitForOrder() {
        playerOrder = userInterface.waitForOtherPlayers(clientHandler);
        playerShelves = new HashMap<>();
        for (String pl : playerOrder) {
            if (!pl.equals(player.getUsername()))
                playerShelves.put(pl, new Shelf());
        }
        userInterface.showPlayersOrder(playerOrder);
    }
    private static void playTurn() throws ClientDisconnectedException, NoMessageToReadException {
                Message message = new Message(MessageCode.GENERIC_MESSAGE);
                do {
                    try {
                        message = clientHandler.receivingWithRetry(100, WAITING_TIME);
                    } catch (NoMessageToReadException e) {
                        userInterface.printErrorMessage("Stopped receiving turns notifications");
                    } catch (ClientDisconnectedException e) {
                        userInterface.printErrorMessage("Disconnected from the server while waiting for the next turn.");
                    }
                } while (message.getMessageType() != MessageCode.PLAY_TURN && message.getMessageType() != MessageCode.END_GAME);

                if (message.getMessageType() == MessageCode.PLAY_TURN) {
                    board = ((PlayTurn) message).getBoard();
                    ArrayList<Tiles> pickedTiles = new ArrayList<>();
                    if (((PlayTurn) message).getUsername().equals(player.getUsername())) {  //OWN TURN

                        //TEST ACTIONS
                        userInterface.turnNotification(player.getUsername());
                        boolean canContinue = false;
                        while (!canContinue) {
                            String command = userInterface.getCommand();
                            switch (command) {
                                case "board":
                                    userInterface.boardCommand();
                                    break;
                                case "shelf":
                                    userInterface.shelfCommand();
                                    break;
                                case "help":
                                    userInterface.helpCommand();
                                    break;

                                case "take":
                                    try {
                                        pickedTiles = userInterface.takeCommand();
                                    } catch (UnsupportedOperationException ignored) {
                                    }
                                    break;

                                case "insert":
                                    userInterface.insertCommand(pickedTiles);
                                    break;
                                case "done":
                                    canContinue = userInterface.doneCommand();
                                    break;
                                default:
                                    userInterface.unknownCommand();
                            }
                        }

                        clientHandler.sendingWithRetry(new Message(MessageCode.TURN_OVER), ATTEMPTS, WAITING_TIME);
                        //CHECKING GOALS
                        boolean commonReached = false;
                        goalList = new ArrayList<>(List.of(commonGoals.getFirst(), commonGoals.getSecond()));
                        for (int i = 0; i < 2; i++) {
                            if (goalList.get(i).checkGoal(player.getShelf()) == 1) {
                                int goalScore = goalList.get(i).takePoints();
                                if (goalScore > 0) {
                                    commonReached = true;
                                    userInterface.commonGoalReached(i, goalScore);
                                    clientHandler.sendingWithRetry(new CommonGoalReached(i), ATTEMPTS, WAITING_TIME);
                                }
                            }
                        }
                        if (!commonReached)
                            clientHandler.sendingWithRetry(new CommonGoalReached(2), ATTEMPTS, WAITING_TIME);

                        do {
                            message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                        } while (message.getMessageType() != MessageCode.COMMON_GOAL_REACHED);

                        //System.out.println("Common Goals check passed.");

                        //CHECKING SHELF FULLNESS
                        boolean isShelfFull = true;
                        out:
                        for (int i = 0; i < 6; i++) {
                            for (int j = 0; j < 5; j++) {
                                if (player.getShelf().isCellEmpty(i, j)) {
                                    isShelfFull = false;
                                    break out;
                                }
                            }
                        }
                        if (isShelfFull) {
                            userInterface.shelfCompleted();
                            clientHandler.sendingWithRetry(new FullShelf(player.getUsername(), true), ATTEMPTS, WAITING_TIME);
                        } else {
                            //System.out.println("You didn't complete the shelf.");
                            clientHandler.sendingWithRetry(new FullShelf(player.getUsername(), false), ATTEMPTS, WAITING_TIME);
                        }
                        do {
                            message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                        } while (message.getMessageType() != MessageCode.FULL_SHELF);

                        //END OF TURN
                        clientHandler.sendingWithRetry(new Message(MessageCode.TURN_OVER), ATTEMPTS, WAITING_TIME);
                        do {
                            message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                        } while (message.getMessageType() != MessageCode.TURN_OVER);
                        userInterface.turnCompleted();

                        clientHandler.sendingWithRetry(new ShelfCheck(player.getShelf()), ATTEMPTS, WAITING_TIME);
                    } else {    //OTHER PLAYERS TURN
                        String nowPlaying = ((PlayTurn) message).getUsername();
                        userInterface.showWhoIsPlaying(nowPlaying);
                        do {
                            message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                            if (message.getMessageType() == MessageCode.CHOSEN_TILES)
                                try {
                                    board.takeTiles(((ChosenTiles) message).getPlayerMove());
                                } catch (RuntimeException e) {
                                    System.out.println(e.getMessage());
                                }
                            if (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED)
                                userInterface.someoneReachedCommonGoal(((CommonGoalReached) message).getPlayer(),((CommonGoalReached) message).getPosition(), goalList.get(((CommonGoalReached) message).getPosition()).takePoints());
                            if (message.getMessageType() == MessageCode.FULL_SHELF)
                                userInterface.someoneCompletedShelf(((FullShelf) message).getPlayer());
                        } while (message.getMessageType() != MessageCode.TURN_OVER);
                        message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                        if (message.getMessageType() == MessageCode.SHELF_CHECK)
                            playerShelves.put(nowPlaying, ((ShelfCheck) message).getShelf());
                   }
                } else if (message.getMessageType() == MessageCode.END_GAME) {  //END OF GAME
                    gameOn = false;
                    userInterface.finalScore();

                    clientHandler.sendingWithRetry(new ShelfCheck(player.getShelf()), 50, 10);
                    do {
                        message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } while (message.getMessageType() != MessageCode.FINAL_SCORE);
                    ArrayList<Pair<String, Integer>> playerPoints = ((FinalScore) message).getScore();
                    userInterface.finalRank(playerPoints);
                }
            }
}
