package it.polimi.ingsw.model.logic;


import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class: GameLogic
 * @author Mattia Lucamarini
 * This class implements the game rules and manages the players' turns.
 */
public class GameLogic implements Runnable, Logic {
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    private final int TOTAL_GOALS = 12;
    private final ConcurrentHashMap<String, ClientHandler> clientList;
    private HashMap<String, Integer> playerPoints;
    private final int numPlayers;
    private final int gameID;
    private boolean isActive;
    private boolean fullShelf;
    private Board board;
    private Bag bag;
    private Pair<CommonGoalCard, CommonGoalCard> commonGoals;
    private HashMap<String, PersonalGoalCard> personalGoals;
    private ArrayList<String> playerOrder;
    private HashMap<String, Shelf> playerShelves;
    private String nowPlaying;
    ArrayList<Tiles> playerPickTypes = new ArrayList<>();

    public GameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID, Board board){
        this.clientList = clientList;
        this.numPlayers = clientList.size();
        this.gameID = gameID;
        this.isActive = true;
        this.playerPoints = new HashMap<>();
        this.personalGoals = new HashMap<>();
        this.fullShelf = false;
        this.board = board;

        //initialize player shelves
        this.playerShelves = new HashMap<>(numPlayers);
        for (String player : clientList.keySet())
            playerShelves.put(player, new Shelf());

        // Initialize common goals.
        commonGoals = new Pair<>(new CommonGoalCard(numPlayers), new CommonGoalCard(numPlayers));
        while(commonGoals.getFirst().getGoalIndex() == commonGoals.getSecond().getGoalIndex()){
            //System.out.println("Goals index are the same, picking another one");
            commonGoals = Pair.of(commonGoals.getFirst(), new CommonGoalCard(numPlayers));
        }

        // Initialize player points and personal goals.
        PersonalGoalCard.resetGoalDeck();
        for (var username : clientList.keySet()) {
            playerPoints.put(username, 0);
            personalGoals.put(username, new PersonalGoalCard());
        }
    }

    public GameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID){
        this(clientList, gameID, null);
    }

    public Board getBoard() {
        return board;
    }

    public HashMap<String, Integer> getPlayerPoints() {
        return new HashMap<>(playerPoints);
    }

    public Pair<CommonGoalCard, CommonGoalCard> getCommonGoals() {
        return new Pair<>(commonGoals.getFirst(), commonGoals.getSecond());
    }

    @Override
    public void run(){
        System.out.println("\nPreparing [GAME " + gameID + "]");
        this.bag = new Bag();

        //DISTRIBUTE TILES
        if (board == null) {
            this.board = new Board(numPlayers);
            board.refillBoard();
        }

        //SEND PERSONAL AND COMMON GOALS
        sendGoalsToClients();

        //CHOOSE FIRST PLAYER
        playerOrder = new ArrayList<>(clientList.keySet().stream().toList());
        Collections.shuffle(playerOrder);
        System.out.print("\n[GAME " + gameID + "] Player order: ");
        for (String pl : playerOrder)
            System.out.print(pl + " ");
        System.out.println();

        //START GAME
        System.out.println("[GAME " + gameID + "] Now starting..");
        for (String username : clientList.keySet()){
            try {
                clientList.get(username).sendingWithRetry(new PlayerOrder(playerOrder), ATTEMPTS, WAITING_TIME);
                clientList.get(username).sendingWithRetry(new Message(MessageCode.GAME_START), ATTEMPTS, WAITING_TIME);
            } catch (ClientDisconnectedException e){
                System.out.println(username + " disconnected while sending game start notification");
            }
        }

        //START TURNS
        while (!fullShelf){
            System.out.println("[GAME " + gameID + "] Starting round");
            for (String pl : playerOrder) {
                nowPlaying = pl;
                playTurn(pl);
            }
        }

        //GAME END, CREATE PLAYER RANKING AND SEND TO ALL CLIENTS
        System.out.println("\n[GAME " + gameID + "] All turns are over. Calculating score..");
        for (String pl : playerOrder)
            assignPoints(pl);
        System.out.println("\n[GAME " + gameID + "] FINAL SCORES: ");
        ArrayList<Pair<String, Integer>> orderedPoints = new ArrayList<>();
        for (Map.Entry<String, Integer> score : playerPoints.entrySet())
            orderedPoints.add(Pair.of(score.getKey(), score.getValue()));

        orderedPoints.sort(Comparator.comparing(Pair::getSecond));
        Collections.reverse(orderedPoints);
        for (int i = 0; i < orderedPoints.size(); i++)
            System.out.println(i+1+": "+ orderedPoints.get(i).getFirst() + " (" + orderedPoints.get(i).getSecond()+" punti)");
        System.out.println(orderedPoints.get(0).getFirst() + " wins!");
        for (String pl : clientList.keySet()) {
            try {
                clientList.get(pl).sendingWithRetry(new FinalScore(orderedPoints), ATTEMPTS, WAITING_TIME);
            } catch (ClientDisconnectedException e) {
                System.out.println(pl + " disconnected while sending final score.");
            }
        }
    }

    public void sendGoalsToClients() {
        for (String username : clientList.keySet()){
            // Send personal and common goals.
            try {
                System.out.println("[GAME " + gameID + "] Sending goals to " + username);
                // TODO: Why call the constructor again? Shouldn't it be the same from the hashmap?
                clientList.get(username)
                        .sendingWithRetry(new SetPersonalGoal(new PersonalGoalCard().getGoalIndex()), 100, 1);
                clientList.get(username)
                        .sendingWithRetry(new SetCommonGoals(new Pair<>(commonGoals.getFirst().getGoalIndex(),commonGoals.getSecond().getGoalIndex()), numPlayers), 100, 1);
                //System.out.println("[GAME " + gameID + "] Sent Personal goal to " + username);
            }
            catch (ClientDisconnectedException e){
                System.out.println("[GAME " + gameID + "] Couldn't send Personal Goal to " + username);
            }

            // Wait for awk from clients.
            try {
                Message reply = clientList.get(username).receivingWithRetry(10, 5);
                if (!reply.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL) || !((SetPersonalGoal) reply).getReply())
                    throw new NoMessageToReadException();
                else
                    System.out.println("[GAME " + gameID + "] " + username + " is ready");
            } catch (ClientDisconnectedException cde){
                System.out.println("[GAME " + gameID + "] Client Disconnected after receiving Personal Goal");
            } catch (NoMessageToReadException nme){
                System.out.println("[GAME " + gameID + "] No Personal Goal confirmation was received");
            }
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean reconnectPlayer(String username, ClientHandler clientHandler, String playerStatus) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        //System.out.println("\t[GAME " + gameID + "] Sending response");
        try {
            clientHandler.sendingWithRetry(new Reconnect(personalGoals.get(username).getGoalIndex(), new Pair<>(commonGoals.getFirst().getGoalIndex(), commonGoals.getSecond().getGoalIndex()), numPlayers, playerOrder, nowPlaying, playerShelves), ATTEMPTS, WAITING_TIME);
            //System.out.println("\tSent reconnect message");
            clientHandler.sendingWithRetry(new Message(MessageCode.GAME_START), ATTEMPTS, WAITING_TIME);
            //System.out.println("\tSent game start");
            clientList.replace(username, clientHandler);
            return true;
        } catch (ClientDisconnectedException e) {
            System.out.println("\t[GAME " + gameID + "] " + username + " is still disconnected: ");
            return false;
        }
    }

    public void playTurn(String player) {
        // Broadcast player turn to others
        try {
            if (!clientList.get(player).isConnected())
                throw new ClientDisconnectedException();
        } catch (ClientDisconnectedException e){
            System.out.println("[GAME " + gameID + "] Giving control to next player while " + player + " is out.");
            try {Thread.sleep(1000);}
            catch (InterruptedException ignored){}
            return;
        }
        Message message = new PlayTurn(player);
        ((PlayTurn) message).setBoard(board);
        System.out.println("[GAME " + gameID + "] " + player+", it's your turn.");
        for (String username : clientList.keySet()) {
            if (clientList.get(username).isConnected()) {
                try {
                    clientList.get(username).sendingWithRetry(message, ATTEMPTS, WAITING_TIME);
                    //System.out.println("Sent turn notification to " + username);
                } catch (ClientDisconnectedException e) {
                    System.out.println("[GAME " + gameID + "] " + username + " was disconnected while sending turn start notification");
                } catch (ClassCastException e) {
                    System.out.println("[GAME " + gameID + "] " + e);
                }
            }
        }

        try {
            boolean moveNotificationReceived = false;
            boolean goalNotificationReceived = false;
            boolean fullShelfNotificationReceived = false;
            boolean turnOverNotificationReceived = false;
            int pickedTiles = 0;
            ArrayList<Pair<Integer, Integer>> playerPick = new ArrayList<>();


            // Make client pick tiles from board until turn end.
            // TODO: Player shouldn't be able to pick multiple disconnected tile-rows during the same turn.
            try {
                while (!moveNotificationReceived) {
                    message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                    if (message.getMessageType() == MessageCode.CHOSEN_TILES && pickedTiles <= 3) {
                        try {
                            pickedTiles += ((ChosenTiles) message).getPlayerMove().size();
                            if (pickedTiles > 3)
                                throw new RuntimeException("Too many tiles (" + pickedTiles + ")");
                            playerPickTypes = (ArrayList<Tiles>) board.takeTiles(((ChosenTiles) message).getPlayerMove());
                            clientList.get(player).sendingWithRetry(new Message(MessageCode.MOVE_LEGAL), ATTEMPTS, WAITING_TIME);
                            playerPick.addAll(((ChosenTiles) message).getPlayerMove());
                            System.out.print("[GAME " + gameID + "] "+ player + " took [");
                            for (Tiles tile : playerPickTypes)
                                System.out.print(" " + tile);
                            System.out.println(" ]");
                        } catch (RuntimeException e) {
                            System.out.println(player + " made an illegal move. (" + e.getMessage() + ")");
                            clientList.get(player).sendingWithRetry(new Message(MessageCode.MOVE_ILLEGAL), ATTEMPTS, WAITING_TIME);
                        }
                    }
                    else if (message.getMessageType() == MessageCode.TURN_OVER) {
                        for (String username : clientList.keySet()) {
                            if (!username.equals(player))
                                clientList.get(username).sendingWithRetry(new ChosenTiles(playerPick), ATTEMPTS, WAITING_TIME);
                        }
                        moveNotificationReceived = true;
                    }
                }
            } catch (ClientDisconnectedException e) {
                System.out.println("[GAME " + gameID + "] " + player + " disconnected during their turn.");
                if (!playerPick.isEmpty() && !playerPickTypes.isEmpty())
                    board.putItBack(playerPick, playerPickTypes);
                return;
            } catch (NoMessageToReadException ignored){}

            // Check if player completes common goal.
            while (!goalNotificationReceived) {
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED) {
                    goalNotificationReceived = true;
                    // NB. 2 means goal not reached.
                    if (((CommonGoalReached) message).getPosition() != 2) {
                        System.out.println("[GAME " + gameID + "] " + player + " completed goal " + ((CommonGoalReached) message).getPosition());
                        switch (((CommonGoalReached) message).getPosition()) {
                            case 0 -> playerPoints.put(player, playerPoints.get(player) + commonGoals.getFirst().getGoal().takePoints());
                            case 1 -> playerPoints.put(player, playerPoints.get(player) + commonGoals.getSecond().getGoal().takePoints());
                            default -> throw new UnsupportedOperationException();
                        }
                        for (String username : clientList.keySet())
                            try {
                                clientList.get(username).sendingWithRetry(new CommonGoalReached(player, ((CommonGoalReached) message).getPosition()), ATTEMPTS, WAITING_TIME);
                            } catch (ClientDisconnectedException e) {
                                System.out.println("[GAME " + gameID + "] " + username + " disconnected while sending Common Goal notification");
                            }
                    } else {
                        System.out.println("[GAME " + gameID + "] " + player + " did not complete any goal");
                        clientList.get(player).sendingWithRetry(new CommonGoalReached(2), ATTEMPTS, WAITING_TIME);
                    }
                }
            }

            // Check if player has completed shelf (NB. game end).
            while (!fullShelfNotificationReceived) {
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.FULL_SHELF && ((FullShelf) message).getOutcome()) {
                    fullShelf = true;
                    fullShelfNotificationReceived = true;
                    System.out.println("[GAME " + gameID + "] " + player + " completed their shelf!");
                    playerPoints.put(player, playerPoints.get(player) + 1);
                    for (String username : clientList.keySet()) {
                        try {
                            clientList.get(username).sendingWithRetry(new FullShelf(player, true), ATTEMPTS, WAITING_TIME);
                        } catch (ClientDisconnectedException e) {
                            System.out.println("[GAME " + gameID + "] " + username + " disconnected while sending Full Shelf notification");
                        }
                    }
                }
                else if (message.getMessageType() == MessageCode.FULL_SHELF && !((FullShelf) message).getOutcome()) {
                    fullShelfNotificationReceived = true;
                    System.out.println("[GAME " + gameID + "] " + player + " didn't complete the shelf.");
                    clientList.get(player).sendingWithRetry(new FullShelf(player, false), ATTEMPTS, WAITING_TIME);
                }
            }

            // Wait for turn over message from player to end turn.
            while (!turnOverNotificationReceived) {
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.TURN_OVER) {
                    turnOverNotificationReceived = true;
                    for (String username : clientList.keySet()) {
                        try {
                            clientList.get(username).sendingWithRetry(new Message(MessageCode.TURN_OVER), ATTEMPTS, WAITING_TIME);
                        } catch (ClientDisconnectedException e) {
                            System.out.println("[GAME " + gameID + "] " + username + " disconnected while sending End Turn notification");
                        }
                    }
                }
            }
            // Wait for updated shelf from player (send to other players to update their clients).
            while (message.getMessageType() != MessageCode.SHELF_CHECK) {
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.SHELF_CHECK) {
                    playerShelves.put(player, ((ShelfCheck) message).getShelf());
                    for (String pl : clientList.keySet()) {
                        if (!pl.equals(player))
                            clientList.get(pl).sendingWithRetry(new ShelfCheck(((ShelfCheck) message).getShelf()), ATTEMPTS, WAITING_TIME);
                    }
                    playerPickTypes.clear();
                }
            }
        }
        catch (NoMessageToReadException ignored) {}
        catch (ClientDisconnectedException e) {
            System.out.println("[GAME " + gameID + "] " + player + " disconnected.");
        }
    }

    public void assignPoints(String player) {
        // Send END_GAME msg and wait for SHELF_CHECK msg with Shelf info.
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        do {
            try {
                clientList.get(player).sendingWithRetry(new Message(MessageCode.END_GAME), ATTEMPTS, WAITING_TIME);
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
            } catch (NoMessageToReadException e){
                System.out.println("[GAME " + gameID + "] Didn't receive shelf from " + player);
            } catch (ClientDisconnectedException e){
                System.out.println("[GAME " + gameID + "]" + player + " disconnected during point assignment.");
            }
        } while (message.getMessageType() != MessageCode.SHELF_CHECK);

        // Calculate personal goal scores on shelf.
        int personalGoalScore = personalGoals.get(player).getGoal().checkGoal(((ShelfCheck) message).getShelf());
        playerPoints.put(player, playerPoints.get(player) + personalGoalScore);
        System.out.println("\n[GAME " + gameID + "] " + player + " has gained " + personalGoalScore + " points from their personal goal.");

        // Calculate points due to same color groups on shelf.
        var groups =  ((ShelfCheck) message).getShelf().findTileGroups();
        for (var group : groups) {
            int gainedPoints = Shelf.scoreGroup(group);
            playerPoints.put(player, playerPoints.get(player) + gainedPoints);
            System.out.println("[GAME " + gameID + "] " + player + " has gained " + gainedPoints + " points, having made a group of " + group.getSecond() + " tiles.");
        }
    }
}
