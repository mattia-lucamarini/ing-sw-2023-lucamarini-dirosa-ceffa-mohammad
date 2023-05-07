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
import java.util.regex.Pattern;

/**
 * Class: Client
 * This class manages all the requests between the players and the game server.
 * @author Mattia Lucamarini
 */
public class Client {
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    private static Player player;
    private static PersonalGoalCard goalCard;
    private static Pair<CommonGoal, CommonGoal> commonGoals;
    private static ArrayList<CommonGoal> goalList;
    private static Board board;
    private static boolean gameOn;
    public static void main(String[] args) {
        System.out.print("Insert username: ");
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
                System.out.print("Insert player number: ");
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
                while (gameOn) {
                    do {
                        try {
                            message = clientHandler.receivingWithRetry(100, WAITING_TIME);
                        } catch (NoMessageToReadException e) {
                            System.out.println("Stopped receiving turns notifications");
                            clientHandler.stopConnection();
                            return;
                        } catch (ClientDisconnectedException e) {
                            System.out.println("Disconnected from the server while waiting for the next turn.");
                            clientHandler.stopConnection();
                            return;
                        }
                    } while (message.getMessageType() != MessageCode.PLAY_TURN && message.getMessageType() != MessageCode.END_GAME);

                    if (message.getMessageType() == MessageCode.PLAY_TURN) {
                        board = ((PlayTurn) message).getBoard();
                        if (((PlayTurn) message).getUsername().equals(player.getUsername())) {
                            System.out.println("\nIt's your turn!");

                            //TEST ACTIONS
                            ArrayList<Pair<Integer, Integer>> tempPick = new ArrayList<>();
                            ArrayList<Pair<Integer, Integer>> totalPick = new ArrayList<>();
                            ArrayList<Tiles> pickedTiles  = new ArrayList<>();
                            Pattern tilePattern;
                            System.out.println("Enter a command to play. (type 'help' to see all commands)");
                            String command;
                            boolean canContinue = true;
                            do {
                                System.out.print("> ");
                                command = sc.nextLine();
                                switch (command) {
                                    case "board":
                                        board.printBoard();
                                        break;
                                    case "shelf":
                                        player.getShelf().printShelf();
                                        break;
                                    case "help":
                                        System.out.println("""

                                                board: print board
                                                shelf: print shelf
                                                take: extract the tiles specified by your coordinates
                                                insert: put your tiles into the shelf""");
                                        break;
                                    case "take":
                                        if (totalPick.size() < 3) {
                                            System.out.println("Type the coordinates of the tile you want to take (ex. 3 2)\n Type cancel to redo your move\n Type nothing if you are done.");
                                            for (int i = totalPick.size(); i < 3 && totalPick.size() < 3; i++) {
                                                System.out.print("\t" + (i + 1) + "> ");
                                                String tilePick = sc.nextLine();
                                                tilePattern = Pattern.compile("[0-9]\\s+[0-9]");
                                                if (tilePick.equals("cancel")) {
                                                    tempPick.clear();
                                                    break;
                                                } else if (tilePattern.matcher(tilePick).find()) {
                                                    Scanner pickScanner = new Scanner(tilePick);
                                                    tempPick.add(Pair.of(pickScanner.nextInt(), pickScanner.nextInt()));
                                                } else if (tilePick.equals("") && tempPick.size() > 0)
                                                    break;
                                                else {
                                                    System.out.println("\tInvalid command. Type the row, followed by whitespace and the column.");
                                                    i--;
                                                }
                                            }
                                        } else {
                                            System.out.println("You already took 3 tiles.");
                                            break;
                                        }
                                        if (tempPick.size() > 0) {
                                            try {
                                                pickedTiles.addAll(board.takeTiles(tempPick));
                                                //System.out.println(pickedTiles);
                                                totalPick.addAll(tempPick);
                                                tempPick.clear();
                                                do {
                                                    clientHandler.sendingWithRetry(new ChosenTiles(totalPick), ATTEMPTS, WAITING_TIME);
                                                    message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                                                } while (message == null);
                                                if (message.getMessageType() == MessageCode.MOVE_LEGAL) {
                                                    System.out.println("Move was verified.");
                                                } else
                                                    System.out.println("Move was not verified.");
                                            } catch (RuntimeException e) {
                                                System.out.println(e.getMessage());
                                                System.out.println("Try another move.");
                                                totalPick.clear();
                                            }
                                            break;
                                        }
                                        break;
                                    case "insert":
                                        if (pickedTiles.size() == 0) {
                                            System.out.println("You have no available tiles to insert.");
                                            break;
                                        }
                                        System.out.println("Type <index> <row> <column> to insert the picked tiles in your shelf.");
                                        System.out.println("Your tiles: ");
                                        for (int i = 0; i < pickedTiles.size(); i++)
                                            System.out.println(i + ": " + pickedTiles.get(i));

                                        tilePattern = Pattern.compile("[0-2]\\s+[0-5]\\s+[0-4]");
                                        System.out.print("\t> ");
                                        String shelfMove = sc.nextLine();
                                        if (shelfMove.equals("cancel")) {
                                            break;
                                        } else if (tilePattern.matcher(shelfMove).find() && totalPick.size() <= 3) {
                                            Scanner pickScanner = new Scanner(shelfMove);
                                            int moveIndex = pickScanner.nextInt();
                                            Tiles pick = pickedTiles.get(moveIndex);
                                            Pair<Integer, Integer> pickPosition = Pair.of(pickScanner.nextInt(), pickScanner.nextInt());
                                            try{
                                                player.getShelf().insertTiles(new ArrayList<>(List.of(pickPosition)), new ArrayList<>(List.of(pick)));
                                                pickedTiles.remove(moveIndex);
                                                System.out.println("Done.");
                                            } catch (RuntimeException e){
                                                System.out.println(e.getMessage());
                                            }
                                        }
                                        break;
                                    case "done":
                                        if (pickedTiles.size() > 0) {
                                            System.out.println("You still have to insert your tiles first.");
                                            canContinue = false;
                                        } else
                                            canContinue = true;
                                        break;
                                    default:
                                        System.out.println("Unknown command.");
                                }
                            } while (!command.equals("done") || !canContinue);

                            clientHandler.sendingWithRetry(new Message(MessageCode.TURN_OVER), ATTEMPTS, WAITING_TIME);
                            //CHECKING GOALS
                            boolean commonReached = false;
                            goalList =  new ArrayList<>(List.of(commonGoals.getFirst(), commonGoals.getSecond()));
                            for (int i = 0; i < 2; i++) {
                                if (goalList.get(i).checkGoal(player.getShelf()) == 1) {
                                    int goalScore = goalList.get(i).takePoints();
                                    if (goalScore > 0) {
                                        commonReached = true;
                                        System.out.println("You reached common goal " + i + ", gaining " + goalScore +" points.");
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
                                System.out.println("You completed the shelf and gained 1 point.");
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
                            System.out.println("You completed your turn.");

                        } else {
                            System.out.println(((PlayTurn) message).getUsername() + " is now playing.");
                            do {
                                message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                                if (message.getMessageType() == MessageCode.CHOSEN_TILES)
                                    try {
                                        board.takeTiles(((ChosenTiles) message).getPlayerMove());
                                    } catch (RuntimeException e){
                                        System.out.println(e.getMessage());
                                    }
                                if (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED)
                                    System.out.println(((CommonGoalReached) message).getPlayer() + " reached Common Goal " + ((CommonGoalReached) message).getPosition() + ", gaining " + goalList.get(((CommonGoalReached) message).getPosition()).takePoints() + "points.");
                                if (message.getMessageType() == MessageCode.FULL_SHELF)
                                    System.out.println(((FullShelf) message).getPlayer() + " completed their shelf, obtaining 1 point! \nRemaining players will play their turns before calculating the score and ending the game.");
                            } while (message.getMessageType() != MessageCode.TURN_OVER);
                        }
                    } else if (message.getMessageType() == MessageCode.END_GAME){
                        gameOn = false;
                        System.out.println("\nThe game is over.\n");
                        System.out.println("I gained "+ goalCard.getGoal().checkGoal(player.getShelf()) + " points from my personal goal.");
                        ArrayList<Pair<Tiles, Integer>> tileGroups = (ArrayList<Pair<Tiles, Integer>>) player.getShelf().findTileGroups();

                        for (Pair<Tiles, Integer> group : tileGroups){
                            int gainedPoints = 0;
                            if (group.getSecond() == 3)
                                gainedPoints = 2;
                            else if (group.getSecond() == 4)
                                gainedPoints = 3;
                            else if (group.getSecond() == 5)
                                gainedPoints = 5;
                            else if (group.getSecond() >= 6)
                                gainedPoints = 8;
                            System.out.println("I gained " + gainedPoints + " points, having made a group of " + group.getSecond() + " tiles.");
                        }

                        clientHandler.sendingWithRetry(new ShelfCheck(player.getShelf()), 50, 10);
                        do {
                            message = clientHandler.receivingWithRetry(ATTEMPTS, WAITING_TIME);
                        } while (message.getMessageType() != MessageCode.FINAL_SCORE);
                        System.out.println("\nFINAL SCORES:");
                        ArrayList<Pair<String, Integer>> playerPoints = ((FinalScore) message).getScore();
                        for (int i = 0; i < playerPoints.size(); i++)
                            System.out.println(i+1+": "+ playerPoints.get(i).getFirst() + " (" + playerPoints.get(i).getSecond()+" points)");
                        System.out.println(playerPoints.get(0).getFirst() + " wins!");
                    }
                }
            }

        } catch (Exception ignored){}
    }
}
