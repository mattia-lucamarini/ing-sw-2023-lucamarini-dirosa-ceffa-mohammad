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
    private final int TOTAL_GOALS = 12;
    private final ConcurrentHashMap<String, ClientHandler> clientList;
    private HashMap<String, Integer> playerPoints;
    private final int numPlayers;
    private final int gameID;
    private boolean isActive;
    private Board board;
    private Bag bag;
    private Pair<CommonGoalCard, CommonGoalCard> commonGoals;
    private List<String> playerOrder;

    public GameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID){
        this.clientList = clientList;
        this.numPlayers = clientList.size();
        this.gameID = gameID;
        this.isActive = true;
        this.playerPoints = new HashMap<>();
    }
    @Override
    public void run(){
        System.out.println("\nPreparing game " + gameID);
        this.board = new Board(numPlayers);
        this.bag = new Bag();

        //SEND PERSONAL AND COMMON GOALS
        commonGoals = new Pair<>(new CommonGoalCard(numPlayers), new CommonGoalCard(numPlayers));
        for (String username : clientList.keySet()){
            try {
                playerPoints.put(username, 0);
                System.out.println("Sending goals to " + username);
                clientList.get(username)
                        .sendingWithRetry(new SetPersonalGoal(new PersonalGoalCard().getGoalIndex()), 100, 1);
                clientList.get(username)
                        .sendingWithRetry(new SetCommonGoals(new Pair<>(commonGoals.getFirst().getGoalIndex(),commonGoals.getSecond().getGoalIndex()), numPlayers), 100, 1);
                //System.out.println("Sent Personal goal to " + username);
            }
            catch (ClientDisconnectedException e){
                System.out.println("Couldn't send Personal Goal to " + username);
            }
            try {
                Message reply = clientList.get(username).receivingWithRetry(10, 5);
                if (!reply.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL) || !((SetPersonalGoal) reply).getReply())
                    throw new NoMessageToReadException();
                else
                    System.out.println(username + " is ready");
            } catch (ClientDisconnectedException cde){
                System.out.println("Client Disconnected after receiving Personal Goal");
            } catch (NoMessageToReadException nme){
                System.out.println("No Personal Goal confirmation was received");
            }
        }
        //DISTRIBUTE TILES
        board.refillBoard();
        //START GAME
        System.out.println("Game " + gameID + " can now start");
        for (String username : clientList.keySet()){
            try {
                clientList.get(username).sendingWithRetry(new Message(MessageCode.GAME_START), 50, 10);
            } catch (ClientDisconnectedException e){
                System.out.println(username + "disconnected while sending game start notification");
            }
        }
        //CHOOSE FIRST PLAYER
        playerOrder = new ArrayList<>(clientList.keySet().stream().toList());
        Collections.shuffle(playerOrder);
        System.out.print("\nPlayer order for game "+gameID+": ");
        for (String pl : playerOrder)
            System.out.print(pl+" ");
        System.out.println();
        playTurn(playerOrder.get(0));
    }
    @Override
    public boolean isActive() {
        return isActive;
    }
    public void playTurn(String player){
        System.out.println(player+", it's your turn.");
        for (String username : clientList.keySet()){
            try {
                clientList.get(username).sendingWithRetry(new PlayTurn(player), 50, 10);
            } catch (ClientDisconnectedException e) {
                System.out.println(username + "disconnected while sending turn notification");
            }
        }
        Message message = new Message(MessageCode.GENERIC_MESSAGE);
        do {
            try{
                message = clientList.get(player).receivingWithRetry(50, 10);
                if (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED) {
                    System.out.println(player + " reached goal "+ ((CommonGoalReached) message).getPosition());
                    switch (((CommonGoalReached) message).getPosition()) {
                        case 0 ->
                                playerPoints.put(player, playerPoints.get(player) + commonGoals.getFirst().getGoal().takePoints());
                        case 1 ->
                                playerPoints.put(player, playerPoints.get(player) + commonGoals.getSecond().getGoal().takePoints());
                        default -> throw new UnsupportedOperationException();
                    }
                    break;
                }
                System.out.println(player + " reached goal " + ((CommonGoalReached) message).getPosition() + 1);
            } catch (NoMessageToReadException ignored){}
            catch (ClientDisconnectedException e){
                System.out.println(player + " disconnected.");
                return;
            }
        }
        while (message.getMessageType() == MessageCode.COMMON_GOAL_REACHED);
    }
    public void assignPoints(String player){
        //check game end
        //check personal goal
        //check common goal
        //check adjacent groups
    }
    public void finishGame(){
        //assign points to players
        //declare winner
    }
}
