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
 * This class implements the game rules, manages the players' turns, and calculates the ending score.
 */
public class GameLogic implements Runnable, Logic {
    public final static int MAX_PLAYERS = 4;
    private static final int ATTEMPTS = 25;
    private static final int WAITING_TIME = 5;
    private final int TOTAL_GOALS = 12;
    private final ConcurrentHashMap<String, ClientHandler> clientList;
    private Set<String> disconnectedPlayers;
    private HashMap<String, Integer> playerPoints;
    private final int numPlayers;
    private final int gameID;
    private boolean isActive;
    private boolean fullShelf;
    private Board board;
    private Pair<CommonGoalCard, CommonGoalCard> commonGoals;
    private HashMap<String, PersonalGoalCard> personalGoals;
    private ArrayList<String> playerOrder;
    private HashMap<String, Shelf> playerShelves;
    private String nowPlaying;

    /**
     * The constructor is used by the server to instantiate new games.
     * @param clientList Contains all the players and their clienthandlers.
     * @param gameID A unique number used by the server to manage multiple games.
     * @param board The playing board assigned to this game.
     */
    public GameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID, Board board){
        this.clientList = clientList;
        this.disconnectedPlayers = new HashSet<>();
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
        System.out.println("\n[GAME " + gameID + "] Initializing data structures..");

        //DISTRIBUTE TILES
        if (board == null) {
            this.board = new Board(numPlayers);
            board.refillBoard();
        }

        //SEND PERSONAL AND COMMON GOALS
        sendGoalsToClients();

        //RANDOMIZE PLAYER ORDER
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
                disconnectedPlayers.add(username);
            }
        }

        //START TURNS
        //The game continues until a player fills their shelf
        while (!fullShelf){
            System.out.println("[GAME " + gameID + "] Starting round");
            for (String pl : playerOrder) {
                nowPlaying = pl;
                if (playTurn(pl)) //playTurn returns true if the game ended for any other reasons (ex. everyone disconnected)
                    return;
            }
        }

        //GAME END
        System.out.println("\n[GAME " + gameID + "] All turns are over. Calculating score..");
        for (String pl : playerOrder)
            assignPoints(pl, false);
        System.out.println("\n[GAME " + gameID + "] FINAL SCORES: ");

        //The final scores are transferred from a HashMap to a List, then finally sorted by decreasing order
        ArrayList<Pair<String, Integer>> orderedPoints = new ArrayList<>();
        for (Map.Entry<String, Integer> score : playerPoints.entrySet())
            orderedPoints.add(Pair.of(score.getKey(), score.getValue()));

        orderedPoints.sort(Comparator.comparing(Pair::getSecond));
        Collections.reverse(orderedPoints);

        for (int i = 0; i < orderedPoints.size(); i++)
            System.out.println(i+1+": "+ orderedPoints.get(i).getFirst() + " (" + orderedPoints.get(i).getSecond()+" punti)");
        System.out.println("[GAME " + gameID + "] OVER: " + orderedPoints.get(0).getFirst() + " wins!");

        //Sends the final score to every player
        for (String pl : clientList.keySet()) {
            try {
                clientList.get(pl).sendingWithRetry(new FinalScore(orderedPoints), ATTEMPTS, WAITING_TIME);
            } catch (ClientDisconnectedException e) {
                System.out.println(pl + " disconnected while sending final score.");
                disconnectedPlayers.add(pl);
            }
        }
        isActive = false;
    }

    /**
     * Sends personal and common goals to every player, it is only called at the start of the game.
     */
    public void sendGoalsToClients() {
        for (String username : clientList.keySet()){
            try {
                System.out.println("[GAME " + gameID + "] Sending goals to " + username);

                clientList.get(username).sendingWithRetry(
                        new SetPersonalGoal(personalGoals.get(username).getGoalIndex()), 100, 1);
                clientList.get(username).sendingWithRetry(
                        new SetCommonGoals(
                                new Pair<>(
                                        commonGoals.getFirst().getGoalIndex(),
                                        commonGoals.getSecond().getGoalIndex()),
                                numPlayers),
                        100, 1);
                //System.out.println("[GAME " + gameID + "] Sent Personal goal to " + username);
            }
            catch (ClientDisconnectedException e){
                System.out.println("[GAME " + gameID + "] Couldn't send Personal Goal to " + username);
                disconnectedPlayers.add(username);
            }

            // Wait for ack from clients. (In the form of a SetPersonalGoal message)
            try {
                Message reply = clientList.get(username).receivingWithRetry(10, 5);
                if (!reply.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL))
                    throw new NoMessageToReadException();
                else
                    System.out.println("[GAME " + gameID + "] " + username + " is ready");
            } catch (ClientDisconnectedException cde){
                System.out.println("[GAME " + gameID + "] Client Disconnected after receiving Personal Goal");
                disconnectedPlayers.add(username);
            } catch (NoMessageToReadException nme){
                System.out.println("[GAME " + gameID + "] No Personal Goal confirmation was received");
            }
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    /**
     * Used by the server to reconnect players who were already part of a game.
     * @param username the unique identifier of a player
     * @param clientHandler the object to manage the communication. It should be updated in the Game instance
     * @param playerStatus represents at which point of the game the player disconnected
     * @return a boolean value representing the success of the operation
     */
    @Override
    public boolean reconnectPlayer(String username, ClientHandler clientHandler, String playerStatus) {
        //System.out.println("\t[GAME " + gameID + "] Sending response");
        try {
            clientHandler.sendingWithRetry(new Reconnect(personalGoals.get(username).getGoalIndex(),
                    new Pair<>(commonGoals.getFirst().getGoalIndex(), commonGoals.getSecond().getGoalIndex()),
                    numPlayers, playerOrder, nowPlaying, playerShelves, board), ATTEMPTS, WAITING_TIME);
            //The GAME_START message reinserts the client into the game loop
            clientHandler.sendingWithRetry(new Message(MessageCode.GAME_START), ATTEMPTS, WAITING_TIME);

            //Updates the clientHandler and removes the player from the disconnected list.
            clientList.replace(username, clientHandler);
            disconnectedPlayers.remove(username);
            return true;
        } catch (ClientDisconnectedException e) {
            System.out.println("\t[GAME " + gameID + "] " + username + " is still disconnected: ");
            disconnectedPlayers.add(username);
            return false;
        }
    }

    /**
     * Starts and manages every player's turn, processing their moves, checking if any goal was reached
     * and if the shelf was completed.
     * @param player current player username
     * @return a boolean value indicates if the game has to end prematurely. (Everyone else has disconnected)
     */
    public boolean playTurn(String player) {
        // Broadcast player turn to others
        try {
            if (!clientList.get(player).isConnected())  //passes the turn to next player if this one disconnected
                throw new ClientDisconnectedException();
            /*  If the current player is the only one still alive, the server starts a timer of 15 seconds
                and declares the player winner if no one else reconnected at the end of the timer
             */
            else if (disconnectedPlayers.size() == clientList.size() - 1){
                System.out.println("[GAME " + gameID + "] "+ player + " is the only player left. They will win the game if no one reconnects in 15 seconds.");
                try {
                    clientList.get(player).sendingWithRetry(new Message(MessageCode.PLAY_TURN), ATTEMPTS, WAITING_TIME);
                    clientList.get(player).sendingWithRetry(new ForcedWin(false), ATTEMPTS, WAITING_TIME);  //used to notify the player of their possible victory
                } catch (ClientDisconnectedException e){
                    System.out.println("[GAME " + gameID + "] " + player + " disconnected while sending first forced win notification.");
                    System.out.println("[GAME " + gameID + "] Every player disconnected. The game is over.");
                    isActive = false;
                    return true;
                }
                try {Thread.sleep(15 * 1000);}  //START OF TIMER
                catch (InterruptedException ignored){}
                if (disconnectedPlayers.size() == clientList.size() - 1){   //checks if the player is still alone
                    try {
                        //the boolean passed to the message indicates if the player is the winner
                        clientList.get(player).sendingWithRetry(new ForcedWin(true), ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        System.out.println("[GAME " + gameID + "] " + player + " disconnected while sending final forced win notification.");
                        System.out.println("[GAME " + gameID + "] Every player disconnected. GAME OVER.");
                        isActive = false;
                        return true;
                    }
                    System.out.println("[GAME " + gameID + "] Time's up. " + player + " wins!");
                    System.out.println("[GAME " + gameID + "] OVER.");
                    isActive = false;
                    return true;
                } else {
                    System.out.println("[GAME " + gameID + "] Someone reconnected. The games continues.");
                    try {
                        clientList.get(player).sendingWithRetry(new ForcedWin(false), ATTEMPTS, WAITING_TIME);
                    } catch (ClientDisconnectedException e){
                        System.out.println("[GAME " + gameID + "] " + player + " disconnected while sending first forced win notification.");
                        System.out.println("[GAME " + gameID + "] Every player disconnected. GAME OVER.");
                        isActive = false;
                        return true;
                    }
                }
            }
        } catch (ClientDisconnectedException e){    //gets thrown if the current player is disconnected at the start of this turn
            System.out.println("[GAME " + gameID + "] Giving control to next player while " + player + " is out.");
            disconnectedPlayers.add(player);
            //disconnectedPlayers.forEach(System.out::println);
            if (disconnectedPlayers.size() == clientList.size()){
                System.out.println("Everyone disconnected. Nobody wins.");
                System.out.println("[GAME " + gameID + "] OVER.");
                return true;
            }
            /*try {Thread.sleep(1000);}
            catch (InterruptedException ignored){}*/
            return false;
        }
        if(board.checkStatus()){
            System.out.println("Board refilled.");
            board.refillBoard();
        }
        Message message = new PlayTurn(player);
        ((PlayTurn) message).setBoard(board);   //every player receives the updated board at the start of every turn
        System.out.println("[GAME " + gameID + "] " + player+", it's your turn.");
        for (String username : clientList.keySet()) {
            if (clientList.get(username).isConnected()) {
                try {
                    clientList.get(username).sendingWithRetry(message, ATTEMPTS, WAITING_TIME);
                    //System.out.println("Sent turn notification to " + username);
                } catch (ClientDisconnectedException e) {
                    System.out.println("[GAME " + gameID + "] " + username + " was disconnected while sending turn start notification");
                    disconnectedPlayers.add(username);
                } catch (ClassCastException e) {
                    System.out.println("[GAME " + gameID + "] " + e);
                }
            }
        }

        try {
            //these booleans divide the turn into phases, they become true when passing to the next one
            boolean moveNotificationReceived = false;
            boolean goalNotificationReceived = false;
            boolean fullShelfNotificationReceived = false;
            boolean turnOverNotificationReceived = false;
            int pickedTiles = 0;    //number of tiles taken by the client
            ArrayList<Pair<Integer, Integer>> playerPick = new ArrayList<>();   //coordinates of tiles taken
            ArrayList<Tiles> playerPickTypes = new ArrayList<>();   //types of taken tiles
            ArrayList<Pair<Integer, Integer>> insertPosition = new ArrayList<>();   //coordinates of inserted tiles
            ArrayList<Tiles> insertedTiles = new ArrayList<>(); //types of inserted tiles

            try {
                boolean tookTiles = false;
                while (!moveNotificationReceived) { //PLAYER MOVE PROCESSING
                    message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);

                    if (!tookTiles && message.getMessageType() == MessageCode.CHOSEN_TILES && pickedTiles <= 3) {   //TAKE COMMAND
                        try {
                            pickedTiles += ((ChosenTiles) message).getPlayerMove().size();  //checks tiles number
                            if (pickedTiles > 3)
                                throw new RuntimeException("Too many tiles (" + pickedTiles + ")");

                            //test the player move on the server's board. Sends a positive message if the move is ok
                            playerPickTypes = (ArrayList<Tiles>) board.takeTiles(((ChosenTiles) message).getPlayerMove());
                            clientList.get(player).sendingWithRetry(new Message(MessageCode.MOVE_LEGAL), ATTEMPTS, WAITING_TIME);
                            playerPick.addAll(((ChosenTiles) message).getPlayerMove());

                            System.out.print("[GAME " + gameID + "] "+ player + " took [");
                            for (Tiles tile : playerPickTypes)
                                System.out.print(" " + tile);
                            System.out.println(" ]");
                            tookTiles = true;   //this forbids multiple take moves in the same turn
                        } catch (RuntimeException e) {
                            System.out.println(player + " made an illegal move. (" + e.getMessage() + ")");
                            clientList.get(player).sendingWithRetry(new IllegalMove(e.getMessage()), ATTEMPTS, WAITING_TIME);
                        }
                    } else if (tookTiles && message.getMessageType() == MessageCode.INSERT){    //INSERT COMMAND
                        try {
                            //test the player move on the server's shelf. Sends a positive message if the move is ok
                            playerShelves.get(player).insertTiles(((Insert) message).getPositions(), ((Insert) message).getTiles());

                            //used when sending insert move to other players
                            insertPosition = (ArrayList<Pair<Integer, Integer>>) ((Insert) message).getPositions();
                            insertedTiles = (ArrayList<Tiles>) ((Insert) message).getTiles();

                            System.out.println("[GAME " + gameID + "] "+ player + " made a legal insert move.");
                            clientList.get(player).sendingWithRetry(new Message(MessageCode.MOVE_LEGAL), ATTEMPTS, WAITING_TIME);
                        } catch (RuntimeException e){
                            System.out.println("[GAME " + gameID + "] "+ player + " made an illegal insert move.");
                            clientList.get(player).sendingWithRetry(new IllegalMove(e.getMessage()), ATTEMPTS, WAITING_TIME);
                        }
                    } else if (message.getMessageType() == MessageCode.EMPTY){
                        board = new Board(clientList.size());
                        System.out.println("[GAME " + gameID + "] Board is now empty");
                    }
                    else if (message.getMessageType() == MessageCode.TURN_OVER) {
                        for (String username : clientList.keySet()) {
                            //sends the take move to every other player
                            if (!username.equals(player))
                                clientList.get(username).sendingWithRetry(new ChosenTiles(playerPick), ATTEMPTS, WAITING_TIME);
                        }
                        moveNotificationReceived = true;
                    }
                }
            } catch (ClientDisconnectedException e) {
                System.out.println("[GAME " + gameID + "] " + player + " disconnected during their turn.");
                disconnectedPlayers.add(player);
                if (!playerPick.isEmpty() && !playerPickTypes.isEmpty())    //reverts the player take move if they disconnected
                    board.putItBack(playerPick, playerPickTypes);
                return false;
            } catch (NoMessageToReadException ignored){}

            while (!goalNotificationReceived) { //GOAL PROCESSING
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
                if (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED) {
                    goalNotificationReceived = true;
                    // CommonGoalReached has a position attribute which is used as the goal index. 2 means no goal reached.
                    if (((CommonGoalReached) message).getPosition() != 2) {
                        System.out.println("[GAME " + gameID + "] " + player + " completed goal " + ((CommonGoalReached) message).getPosition());
                        switch (((CommonGoalReached) message).getPosition()) {
                            case 0 -> playerPoints.put(player, playerPoints.get(player) + commonGoals.getFirst().getGoal().takePoints());
                            case 1 -> playerPoints.put(player, playerPoints.get(player) + commonGoals.getSecond().getGoal().takePoints());
                            default -> throw new UnsupportedOperationException();
                        }
                        for (String username : clientList.keySet()) //notifies every other player of the reached goal
                            try {
                                clientList.get(username).sendingWithRetry(new CommonGoalReached(player, ((CommonGoalReached) message).getPosition()), ATTEMPTS, WAITING_TIME);
                            } catch (ClientDisconnectedException e) {
                                System.out.println("[GAME " + gameID + "] " + username + " disconnected while sending Common Goal notification");
                                disconnectedPlayers.add(username);
                            }
                    } else {
                        System.out.println("[GAME " + gameID + "] " + player + " did not complete any goal");
                        clientList.get(player).sendingWithRetry(new CommonGoalReached(2), ATTEMPTS, WAITING_TIME);  //notifies every other player of the unreached goal
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
                            disconnectedPlayers.add(username);
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
                            disconnectedPlayers.add(username);
                        }
                    }
                }
            }
            // Send insert move to players.
            for (String pl : clientList.keySet()) {
                if (!pl.equals(player)) {
                    System.out.println("Sending move to " + player);
                    clientList.get(pl).sendingWithRetry(new Insert(insertPosition, insertedTiles), ATTEMPTS, WAITING_TIME);
                    System.out.println("[GAME " + gameID + "] Sent " + player + "'s 'insert' move to " + pl);
                }
            }
            playerPickTypes.clear();
        }
        catch (NoMessageToReadException ignored) {}
        catch (ClientDisconnectedException e) {
            System.out.println("[GAME " + gameID + "] " + player + " disconnected.");
            disconnectedPlayers.add(player);
        }
        return false;
    }

    /**
     * Calculates final score
     * @param player username
     * @param shelfCheck used for testing mode
     */
    public void assignPoints(String player, boolean shelfCheck) {
        Shelf playerShelf = playerShelves.get(player);
        if (shelfCheck){
            Message message = new Message(MessageCode.GENERIC_MESSAGE);
            try {
                message = clientList.get(player).receivingWithRetry(ATTEMPTS, WAITING_TIME);
            } catch (ClientDisconnectedException e){
                System.out.println("Client disconnected while sending test shelf check");
                return;
            } catch (NoMessageToReadException ignored){}
            if (message.getMessageType() == MessageCode.SHELF_CHECK){
                playerShelf = ((ShelfCheck) message).getShelf();
            }
        }
        // Send END_GAME msg
        try {
            clientList.get(player).sendingWithRetry(new Message(MessageCode.END_GAME), ATTEMPTS, WAITING_TIME);
        } catch (ClientDisconnectedException e){
            System.out.println("[GAME " + gameID + "]" + player + " disconnected during point assignment.");
            disconnectedPlayers.add(player);
        }

        // Calculate personal goal scores on shelf.
        int personalGoalScore = personalGoals.get(player).getGoal().checkGoal(playerShelf);
        playerPoints.put(player, playerPoints.get(player) + personalGoalScore);
        System.out.println("\n[GAME " + gameID + "] " + player + " has gained " + personalGoalScore + " points from their personal goal.");

        // Calculate points due to same color groups on shelf.
        var tileGroups = playerShelf.findTileGroups();
        for (var tileGroup : tileGroups) {
            int gainedPoints = Shelf.scoreGroup(tileGroup);
            playerPoints.put(player, playerPoints.get(player) + gainedPoints);
            System.out.println("[GAME " + gameID + "] " + player + " has gained " + gainedPoints + " points, having made a group of " + tileGroup.getSecond() + " tiles.");
        }
    }
}
