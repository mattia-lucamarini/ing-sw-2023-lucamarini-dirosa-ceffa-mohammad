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

    public GameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID, Board board){
        this.clientList = clientList;
        this.numPlayers = clientList.size();
        this.gameID = gameID;
        this.isActive = true;
        this.playerPoints = new HashMap<>();
        this.personalGoals = new HashMap<>();
        this.fullShelf = false;
        this.board = board;
    }

    public GameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID){
        this(clientList, gameID, null);
    }

    public Board getBoard() {
        return board;
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
        commonGoals = new Pair<>(new CommonGoalCard(numPlayers), new CommonGoalCard(numPlayers));
        while(commonGoals.getFirst().getGoalIndex() == commonGoals.getSecond().getGoalIndex()){
            //System.out.println("Goals index are the same, picking another one");
            commonGoals = Pair.of(commonGoals.getFirst(), new CommonGoalCard(numPlayers));
        }
        for (String username : clientList.keySet()){
            try {
                playerPoints.put(username, 0);
                System.out.println("[GAME " + gameID + "] Sending goals to " + username);
                personalGoals.put(username, new PersonalGoalCard());
                clientList.get(username)
                        .sendingWithRetry(new SetPersonalGoal(new PersonalGoalCard().getGoalIndex()), 100, 1);
                clientList.get(username)
                        .sendingWithRetry(new SetCommonGoals(new Pair<>(commonGoals.getFirst().getGoalIndex(),commonGoals.getSecond().getGoalIndex()), numPlayers), 100, 1);
                //System.out.println("[GAME " + gameID + "] Sent Personal goal to " + username);
            }
            catch (ClientDisconnectedException e){
                System.out.println("[GAME " + gameID + "] Couldn't send Personal Goal to " + username);
            }
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
                System.out.println(username + "disconnected while sending game start notification");
            }
        }

        //START TURNS
        while (!fullShelf){
            System.out.println("[GAME " + gameID + "] Starting round");
            for (String pl : playerOrder)
                playTurn(pl);
        }

        System.out.println("\n[GAME " + gameID + "] All turns are over. Calculating score..");
        for (String pl : playerOrder)
            assignPoints(pl);
        System.out.println("\n[GAME " + gameID + "] FINAL SCORES: ");
        ArrayList<Pair<String, Integer>> orderedPoints = new ArrayList<>();
        for (Map.Entry<String, Integer> score : playerPoints.entrySet())
            orderedPoints.add(new Pair(score.getKey(), score.getValue()));

        orderedPoints.sort(Comparator.comparing(Pair::getSecond));
        Collections.reverse(orderedPoints);
        for (int i = 0; i < orderedPoints.size(); i++)
            System.out.println(i+1+": "+ orderedPoints.get(i).getFirst() + " (" + orderedPoints.get(i).getSecond()+" punti)");
        System.out.println(orderedPoints.get(0).getFirst() + " wins!");
        for (String pl : clientList.keySet()) {
            try {
                clientList.get(pl).sendingWithRetry(new FinalScore(orderedPoints), ATTEMPTS, WAITING_TIME);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean reconnectPlayer(String username, ClientHandler clientHandler) { //  -- to implement --
        return false;
    }

    public void playTurn(String player){
        // Broadcast player turn to others
        Message message = new PlayTurn(player);
        ((PlayTurn) message).setBoard(board);
        System.out.println("[GAME " + gameID + "] " + player+", it's your turn.");
        for (String username : clientList.keySet()){
            try {
                clientList.get(username).sendingWithRetry(message, ATTEMPTS, WAITING_TIME);
            } catch (ClientDisconnectedException e) {
                System.out.println("[GAME " + gameID + "] " + username + " disconnected while sending turn start notification");
            } catch (ClassCastException e){
                System.out.println("[GAME " + gameID + "] " + e);
            }
        }

        try{
            boolean moveNotificationReceived = false;
            boolean goalNotificationReceived = false;
            boolean fullShelfNotificationReceived = false;
            boolean turnOverNotificationReceived = false;
            int pickedTiles = 0;
            ArrayList<Pair<Integer, Integer>> playerPick = new ArrayList<>();

            // Make client pick tiles until turn end.
            // TODO: Player shouldn't be able to pick multiple disconnected tile-rows during the same turn.
            while (!moveNotificationReceived){
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.CHOSEN_TILES && pickedTiles <= 3) {
                    try {
                        pickedTiles += ((ChosenTiles) message).getPlayerMove().size();
                            if (pickedTiles > 3)
                                throw new RuntimeException("Too many tiles (" + pickedTiles + ")");
                        board.takeTiles(((ChosenTiles) message).getPlayerMove());
                        clientList.get(player).sendingWithRetry(new Message(MessageCode.MOVE_LEGAL), ATTEMPTS, WAITING_TIME);
                        playerPick.addAll(((ChosenTiles) message).getPlayerMove());
                    } catch (RuntimeException e) {
                        System.out.println(player + " made an illegal move. (" + e.getMessage() + ")");
                        clientList.get(player).sendingWithRetry(new Message(MessageCode.MOVE_ILLEGAL), ATTEMPTS, WAITING_TIME);
                    }
                }
                else if (message.getMessageType() == MessageCode.TURN_OVER){
                    for (String username : clientList.keySet()){
                        if (!username.equals(player))
                            clientList.get(username).sendingWithRetry(new ChosenTiles(playerPick), ATTEMPTS, WAITING_TIME);
                    }
                    moveNotificationReceived = true;
                }
            }
            while (!goalNotificationReceived) {
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED) {
                    goalNotificationReceived = true;
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
            while (message.getMessageType() != MessageCode.SHELF_CHECK) {
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.SHELF_CHECK) {
                    for (String pl : clientList.keySet()) {
                        if (!pl.equals(player))
                            clientList.get(pl).sendingWithRetry(new ShelfCheck(((ShelfCheck) message).getShelf()), ATTEMPTS, WAITING_TIME);
                    }
                }
            }
        } catch (NoMessageToReadException ignored){}
        catch (ClientDisconnectedException e){
            System.out.println("[GAME " + gameID + "] " + player + " disconnected.");
        }
    }

    public void assignPoints(String player){
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
        int personalGoalScore = personalGoals.get(player).getGoal().checkGoal(((ShelfCheck) message).getShelf());
        playerPoints.put(player, playerPoints.get(player) + personalGoalScore);
        System.out.println("\n[GAME " + gameID + "] " + player + " has gained " + personalGoalScore + " points from their personal goal.");

        ArrayList<Pair<Tiles, Integer>> tileGroups = (ArrayList<Pair<Tiles, Integer>>) ((ShelfCheck) message).getShelf().findTileGroups();

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
            playerPoints.put(player, playerPoints.get(player) + gainedPoints);
            System.out.println("[GAME " + gameID + "] " + player + " has gained " + gainedPoints + " points, having made a group of " + group.getSecond() + " tiles.");
        }

    }
}
