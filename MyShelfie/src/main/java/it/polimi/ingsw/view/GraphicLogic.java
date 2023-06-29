package it.polimi.ingsw.view;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiClientHandler;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiInterface;
import it.polimi.ingsw.network.ClientHandler.SocketClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.server.network.RmiServerServices.RmiServerInterface;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;
import it.polimi.ingsw.view.MessageView.MessageCodeView;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GraphicLogic {
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    public static Player player;
    public static ClientHandler clientHandler;
    private static GUIInterface userInterface;
    public static PersonalGoalCard personalGoal;
    private static Pair<CommonGoalCard, CommonGoalCard> commonGoals;
    public static ArrayList<String> playerOrder;
    public static HashMap<String, Shelf> playerShelves;
    public static Board board;
    private static boolean gameOn;
    static boolean someoneDisconnected = false;

    public GraphicLogic(GUIInterface gui){
        this.userInterface=gui;
    }

    public void init(){
        Pair<Pair<String, String>, Pair<String,Integer>> credentials;
        String addressChoice;
        credentials = userInterface.askForUsername();
        player = new Player(credentials.getFirst().getFirst());
        String connection = credentials.getFirst().getSecond();
        addressChoice = credentials.getSecond().getFirst();
        Integer port = credentials.getSecond().getSecond();
        try{
        if(connection.equals("socket")){
            connectSocket(addressChoice, port);
        }
        else if(connection.equals("rmi")){
            connectRMI(addressChoice,port);
        }
        else{
            System.out.println("Invalid option. Defaulting to sockets");
            connectSocket("127.0.0.1", 59090);
        }
        } catch (Exception e){
            System.out.println(e);
        }

        try {
            clientHandler.receivingKernel();
            clientHandler.pingKernel();

            int loginResult;
            do{
                loginResult = login();
            } while (loginResult == 0);
            if (loginResult != 2) {
                while (!goalProcessing())
                    ;
                waitForOrder();
            }

            Message message = new Message(MessageCode.GENERIC_MESSAGE);
            try {
                message = clientHandler.receivingWithRetry(100, 2);
            } catch (NoMessageToReadException e) {
                userInterface.printErrorMessage("Didn't receive start message.");
            } catch (ClientDisconnectedException e) {
                userInterface.printErrorMessage("Disconnected from the server while waiting for other players.");
                System.exit(1);
            }

            if (message.getMessageType() == MessageCode.GAME_START) {
                gameOn = true;
                if(loginResult!=2){
                    //userInterface.showGameStart();
                }

                //TURN PROCESSING
                while (gameOn){
                    playTurn();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    private static void connectRMI(String address, int port){
        Registry registry;
        RmiServerInterface RmiServer;
        RmiInterface rmiClientService;
        boolean connected = false;
        while (!connected) {
            try {
                registry = LocateRegistry.getRegistry(address);
                RmiServer = (RmiServerInterface) registry.lookup("RmiServer");
                rmiClientService = RmiServer.getRmiClientService();
                clientHandler = new RmiClientHandler(rmiClientService);
                connected=true;
            } catch (Exception e) {
                System.out.println("Could not connect to " + address + ":" + port + "\n\tRetrying in 5 seconds..");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored){}
            }
        }
        System.out.println("Successfully connected to " + address + ":" + port);
    }
    private static void connectSocket(String address, int port){
        boolean connected = false;
        while (!connected) {
            try {
                clientHandler = new SocketClientHandler(new Socket(address, port));
                connected = true;
            } catch (UnknownHostException e){
                System.out.println("Could not determine " + address + ":" + port);
                System.exit(12);
            } catch (IOException e) {
                System.out.println("Could not connect to " + address + ":" + port + "\n\tRetrying in 5 seconds..");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored){}
            }
        }
        System.out.println("Successfully connected to " + address + ":" + port);

    }
    private static int login() {
        //LOGIN REQUEST
        boolean flag = false;
        try {
            flag = clientHandler.sendingWithRetry(new LoginRequest(player.getUsername()), ATTEMPTS, WAITING_TIME);
        } catch (ClientDisconnectedException e) {
            userInterface.printErrorMessage("Disconnected from the server before sending log in request.");
            return 0;
        }
        if (!flag) {
            userInterface.printErrorMessage("Can't send the log in request.");
            return 0;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException ignored) {
        }

        //RECEIVING NUMREQUEST
        Message message = null;
        while (message == null){
            try {
                message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
            } catch (NoMessageToReadException e) {
                userInterface.printMessage("No message received after sending the login request");
            } catch (ClientDisconnectedException e) {
                userInterface.printErrorMessage("Disconnected from the server after sending the login request.");
                return 0;
            }
        }

        if (message.getMessageType().equals(MessageCode.NUM_PLAYERS_REQUEST)) {
            try {
                userInterface.askForNumOfPlayers(clientHandler);
            } catch (IOException e) {
                throw new RuntimeException("To implement");
            }

            while (message.getMessageType() != MessageCode.LOGIN_REPLY) {
                try {
                    message = clientHandler.receivingWithRetry(10, 2);
                } catch (NoMessageToReadException e) {
                    userInterface.printMessage("No message received after sending the num player message");
                } catch (ClientDisconnectedException e) {
                    userInterface.printErrorMessage("Disconnected from the server while waiting for log response after num player mess.");
                    return 0;
                }
            }
        }
        //RECEIVING LOGIN REPLY
        if (message.getMessageType().equals(MessageCode.LOGIN_REPLY)) {

            if (((LoginReply) message).getOutcome()) {
                //userInterface.printMessage("\nClient added!");
                userInterface.showGameScene();
                return 1;
            } else {
                Pair<Pair<String, String>, Pair<String,Integer>> credentials;
                String addressChoice;
                userInterface.showLoginScreen();
                credentials = userInterface.askForUsername();
                player = new Player(credentials.getFirst().getFirst());
                String connection = credentials.getFirst().getSecond();
                addressChoice = credentials.getSecond().getFirst();
                Integer port = credentials.getSecond().getSecond();
                try{
                    if(connection.equals("socket")){
                        connectSocket(addressChoice, port);
                    }
                    else if(connection.equals("rmi")){
                        connectRMI(addressChoice,port);
                    }
                    else{
                        System.out.println("Invalid option. Defaulting to sockets");
                        connectSocket("127.0.0.1", 59090);
                    }
                } catch (Exception e){
                    System.out.println(e);
                }
                clientHandler.receivingKernel();
                clientHandler.pingKernel();
                return 0;
            }
            //RECONNECT PLAYER
        }
        else if (message.getMessageType().equals(MessageCode.RECONNECT)) {
            userInterface.showGameScene();
            personalGoal = new PersonalGoalCard(((Reconnect) message).getPersonalGoalIndex());
            commonGoals = new Pair<>(new CommonGoalCard(((Reconnect) message).getNumPlayers(), ((Reconnect) message).getCommonGoalIndexes().getFirst()), new CommonGoalCard(((Reconnect) message).getNumPlayers(), ((Reconnect) message).getCommonGoalIndexes().getSecond()));
            playerOrder = ((Reconnect) message).getPlayerOrder();
            playerShelves = ((Reconnect) message).getPlayerShelves();
            userInterface.printMessage("Welcome back!");
            player.setShelf(((Reconnect) message).getPlayerShelves().get(player.getUsername()));
            board = ((Reconnect)message).getBoard();
            System.out.println();
            userInterface.showPersonalGoal(personalGoal.getGoalIndex());
            System.out.println();
            userInterface.showCommonGoals(commonGoals.getFirst().getGoalIndex(), commonGoals.getSecond().getGoalIndex());
            userInterface.showPlayersOrder(playerOrder);
            userInterface.setPlayersInComboBox(playerOrder);
            userInterface.boardCommand();
            userInterface.updateShelf();
            if (((Reconnect) message).getNowPlaying() != player.getUsername()) {
                System.out.println();
                userInterface.showWhoIsPlaying(((Reconnect) message).getNowPlaying());
            }
            return 2;
        }
        else {
            userInterface.printErrorMessage("Unknown message code received. ("+message.getMessageType()+")");
            return 0;
        }
    }
    private static boolean goalProcessing() {
        //PROCESS PERSONAL GOAL
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        while (personalGoal == null || commonGoals == null) {
            try {
                //System.out.println("Receiving personal goal..");
                message = clientHandler.receivingWithRetry(10, 5);
            } catch (NoMessageToReadException e) {
                userInterface.printMessage("No message received after sending the num player message");
                return false;
            } catch (ClientDisconnectedException e) {
                userInterface.printErrorMessage("Disconnected from the server while waiting for log response after num player mess.");
                System.exit(13);
            }
            if (message.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL)) {
                int goalNumber = ((SetPersonalGoal) message).getGoalNumber();
                userInterface.showPersonalGoal(goalNumber);
                try {
                    clientHandler.sendingWithRetry(new SetPersonalGoal(), 1, 1);
                } catch (ClientDisconnectedException e) {
                    userInterface.printErrorMessage("Disconnected from the server while sending personal goal ack");
                    System.exit(13);
                }
                personalGoal = new PersonalGoalCard(goalNumber);
                /*for (Map.Entry<Pair<Integer, Integer>, Tiles> i : goalCard.getGoal().getConstraint().entrySet())
                    System.out.println(i);*/
            }
            if (message.getMessageType().equals(MessageCode.SET_COMMON_GOALS)) {
                int numPlayers = ((SetCommonGoals) message).getNumPlayers();
                commonGoals = new Pair<>(new CommonGoalCard(numPlayers, ((SetCommonGoals) message).getGoalsIndexes().getFirst()), new CommonGoalCard(numPlayers, ((SetCommonGoals) message).getGoalsIndexes().getSecond()));
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
        System.out.println("size: " + playerShelves.size());
        userInterface.setPlayersInComboBox(playerOrder);
        userInterface.showPlayersOrder(playerOrder);
    }
    private static void playTurn() throws ClientDisconnectedException, NoMessageToReadException, InterruptedException {
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        do {    //WAIT FOR EITHER PLAY_TURN MESSAGE, END_GAME OR FORCED_WIN
            try {
                message = clientHandler.receivingWithRetry(100, WAITING_TIME);
            } catch (NoMessageToReadException e) {
                userInterface.printErrorMessage("Stopped receiving turns notifications");
            } catch (ClientDisconnectedException e) {
                userInterface.printErrorMessage("Disconnected from the server while waiting for the next turn.");
                System.exit(13);
            }
        } while (message.getMessageType() != MessageCode.PLAY_TURN && message.getMessageType() != MessageCode.END_GAME && message.getMessageType() != MessageCode.FORCED_WIN);

        if (message.getMessageType() == MessageCode.FORCED_WIN){
            userInterface.printMessage("\nEveryone else disconnected.\nIf nobody comes back in 15 seconds, you'll be the winner.");
            Message forcedWin = new Message(MessageCode.GENERIC_MESSAGE);
            do {
                try {
                    forcedWin = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                } catch (ClientDisconnectedException e){
                    userInterface.printErrorMessage("Disconnected while waiting for forced win message.");
                    System.exit(13);
                } catch (NoMessageToReadException ignored) {}
            } while (forcedWin.getMessageType() != MessageCode.FORCED_WIN);

            if (((ForcedWin) forcedWin).getWin()){
                userInterface.forceWin(player.getUsername());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored){}
                System.exit(13);
            } else {
                userInterface.printMessage("Someone reconnected. The game continues as usual.");
            }
        }
        if (message instanceof PlayTurn || someoneDisconnected) {
            if (!someoneDisconnected)
                try {
                    board = ((PlayTurn) message).getBoard();
                    userInterface.boardCommand();
                } catch (ClassCastException e){
                    System.out.println("ClassCastException: The client expected a PLAY_TURN message, but received a " + message.getMessageType());
                }
            userInterface.boardCommand();
            userInterface.updateShelf();
            if (someoneDisconnected || ((PlayTurn) message).getUsername().equals(player.getUsername())) {  //OWN TURN
                someoneDisconnected = false;
                //TEST ACTIONS
                userInterface.setIsmyturn(true);
                userInterface.turnNotification(player.getUsername());
                boolean canContinue = false;
                if (!canContinue) {
                    canContinue = userInterface.getCommand();
                                    /*switch (command) {
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
                    }*/
                }
                try {
                    clientHandler.sendingWithRetry(new Message(MessageCode.TURN_OVER), ATTEMPTS, WAITING_TIME);
                } catch (ClientDisconnectedException e){
                    userInterface.printErrorMessage("Disconnected while sending turn over notification.");
                    System.exit(13);
                }
                //CHECKING GOALS
                boolean commonReached = false;
                if (commonGoals.getFirst().getGoal().checkGoal(player.getShelf()) == 1) {
                    int goalScore = commonGoals.getFirst().getGoal().takePoints();
                    if (goalScore > 0) {
                        commonReached = true;
                        userInterface.commonGoalReached(0, goalScore);
                        try {
                            clientHandler.sendingWithRetry(new CommonGoalReached(0), ATTEMPTS, WAITING_TIME);
                        } catch (ClientDisconnectedException e){
                            userInterface.printErrorMessage("Disconnected while sending common goal 0 reached notification.");
                            System.exit(13);
                        }                    }
                }
                if (commonGoals.getSecond().getGoal().checkGoal(player.getShelf()) == 1) {
                    int goalScore = commonGoals.getSecond().getGoal().takePoints();
                    if (goalScore > 0) {
                        commonReached = true;
                        userInterface.commonGoalReached(1, goalScore);
                        try {
                            clientHandler.sendingWithRetry(new CommonGoalReached(1), ATTEMPTS, WAITING_TIME);
                        } catch (ClientDisconnectedException e){
                            userInterface.printErrorMessage("Disconnected while sending common goal 1 reached notification.");
                            System.exit(13);
                        }
                    }
                }
                if (!commonReached)
                    try {
                        clientHandler.sendingWithRetry(new CommonGoalReached(2), ATTEMPTS, WAITING_TIME);   //2 = NO GOAL REACHED
                    } catch (ClientDisconnectedException e){
                        userInterface.printErrorMessage("Disconnected while sending common goal not reached notification.");
                        System.exit(13);
                    }

                do {
                    try {
                        message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        userInterface.printErrorMessage("Disconnected while waiting for common goal ack.");
                        System.exit(13);
                    } catch (NoMessageToReadException ignored) {}
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
                    try {
                        clientHandler.sendingWithRetry(new FullShelf(player.getUsername(), true), ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        userInterface.printErrorMessage("Disconnected while sending full shelf notification.");
                        System.exit(13);
                    }
                } else {
                    //System.out.println("You didn't complete the shelf.");
                    try {
                        clientHandler.sendingWithRetry(new FullShelf(player.getUsername(), false), ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        userInterface.printErrorMessage("Disconnected while sending not full shelf notification.");
                        System.exit(13);
                    }
                }
                do {
                    try {
                        message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        userInterface.printErrorMessage("Disconnected while waiting for full shelf ack.");
                        System.exit(13);
                    } catch (NoMessageToReadException ignored){}
                } while (message.getMessageType() != MessageCode.FULL_SHELF);

                //END OF TURN
                try {
                    clientHandler.sendingWithRetry(new Message(MessageCode.TURN_OVER), ATTEMPTS, WAITING_TIME);
                } catch (ClientDisconnectedException e) {
                    userInterface.printErrorMessage("Disconnected while sending final turn over notification");
                    System.exit(1);
                }
                do {
                    try {
                        message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        userInterface.printErrorMessage("Disconnected while waiting for final turn over notification");
                        System.exit(13);
                    } catch (NoMessageToReadException ignored){}
                } while (message.getMessageType() != MessageCode.TURN_OVER);

                /*try {
                    clientHandler.sendingWithRetry(new ShelfCheck(player.getShelf()), ATTEMPTS, WAITING_TIME);
                    //System.out.println("Sent player shelf");
                } catch (ClientDisconnectedException e) {
                    userInterface.printErrorMessage("Disconnected while sending shelf content");
                    System.exit(1);
                }*/
                userInterface.turnCompleted();

            }else {    //OTHER PLAYERS TURN
                String nowPlaying = ((PlayTurn) message).getUsername();
                userInterface.showWhoIsPlaying(nowPlaying);
                do {
                    try {
                        message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        userInterface.printErrorMessage("Disconnected while waiting for " + nowPlaying + "'s turn.");
                        System.exit(13);
                    } catch (NoMessageToReadException ignored){}
                    if (message.getMessageType() == MessageCode.CHOSEN_TILES)
                        try {
                            board.takeTiles(((ChosenTiles) message).getPlayerMove());
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                    if (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED)
                        if (((CommonGoalReached) message).getPosition() == 0)
                            userInterface.someoneReachedCommonGoal(((CommonGoalReached) message).getPlayer(),((CommonGoalReached) message).getPosition(), commonGoals.getFirst().getGoal().takePoints());
                        else if (((CommonGoalReached) message).getPosition() == 1)

                    if (message.getMessageType() == MessageCode.FULL_SHELF)
                        userInterface.someoneCompletedShelf(((FullShelf) message).getPlayer());
                    if (message.getMessageType() == MessageCode.PLAY_TURN) {
                        userInterface.printMessage(nowPlaying + " disconnected, it's now your turn.");
                        //System.out.println("Received " + message.getMessageType() + " message.");
                        //board = ((PlayTurn) message).getBoard();
                        someoneDisconnected = true;
                        return;
                    }
                } while (message.getMessageType() != MessageCode.TURN_OVER);
                do {
                    try {
                        message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                        if (message.getMessageType() == MessageCode.INSERT) {
                            try {
                                playerShelves.get(nowPlaying).insertTiles(((Insert) message).getPositions(), ((Insert) message).getTiles());
                            } catch (RuntimeException e){
                                System.out.println("Error while updating shelf from + " + nowPlaying + ": " + e.getMessage());
                            }
                            //System.out.println("Received shelf from " + nowPlaying);
                            //((ShelfCheck) message).getShelf().printShelf();
                        }
                    } catch (ClientDisconnectedException e) {
                        System.out.println("Client disconnected while waiting for other shelves.");
                        System.exit(0);
                    } catch (NoMessageToReadException ignored){}
                } while (message.getMessageType() != MessageCode.INSERT);
            }
        } else if (message.getMessageType() == MessageCode.END_GAME) {  //END OF GAME
            gameOn = false;
            userInterface.finalScore();
            try {
                clientHandler.sendingWithRetry(new ShelfCheck(player.getShelf()), 50, 10);
            } catch (ClientDisconnectedException e) {
                System.out.println("Disconnected while sending shelf content during endgame.");
                System.exit(13);
            }
            do {
                try {
                    message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                } catch (ClientDisconnectedException e){
                    userInterface.printErrorMessage("Disconnected while waiting for final score.");
                    System.exit(13);
                } catch (NoMessageToReadException ignored){}
            } while (message.getMessageType() != MessageCode.FINAL_SCORE);
            ArrayList<Pair<String, Integer>> playerPoints = ((FinalScore) message).getScore();
            userInterface.finalRank(playerPoints);
        }
    }
    public int getNumPlayers(){
        return playerOrder.size();
    }
}
