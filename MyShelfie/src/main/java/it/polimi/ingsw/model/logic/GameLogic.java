package it.polimi.ingsw.model.logic;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageCode;
import it.polimi.ingsw.network.message.SetPersonalGoal;
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
    private final int numPlayers;
    private final int gameID;
    private boolean isActive;
    private Board board;
    private Bag tiles;
    private ArrayList<Integer> personalGoals;
    private List<CommonGoal> CommonGoals;
    private List<String> playerOrder;

    public GameLogic(ConcurrentHashMap<String, ClientHandler> clientList, int gameID){
        this.clientList = clientList;
        this.numPlayers = clientList.size();
        this.gameID = gameID;
        this.isActive = true;
        this.personalGoals = new ArrayList<Integer>(List.of(0,1,2,3,4,5,6,7,8,9,10,11,12));
    }
    @Override
    public void run(){
        System.out.println("\nPreparing game " + gameID);
        this.board = new Board(numPlayers);
        this.tiles = new Bag();
        //extract common goals TODO: random

        // TODO: REMOVE THIS: Example to showcase how to choose random common goal
        this.CommonGoals = CommonGoal.all();
        var rand = new Random();
        var randomCommonGoal = this.CommonGoals.get(rand.nextInt(this.CommonGoals.size()));

        //SEND PERSONAL GOALS
        for (String username : clientList.keySet()){
            try {
                System.out.println("Sending Personal goal to " + username);
                int goalNumber = rand.nextInt(TOTAL_GOALS);
                clientList.get(username)
                        .sendingWithRetry(new SetPersonalGoal(goalNumber), 100, 1);
                personalGoals.remove(goalNumber);
                //System.out.println("Sent Personal goal to " + username);
            }
            catch (ClientDisconnectedException e){
                System.out.println("Couldn't send Personal Goal to " + username);
            }
            try {
                Thread.sleep(2000);
                Message reply = clientList.get(username).receive();
                if (!reply.getMessageType().equals(MessageCode.SET_PERSONAL_GOAL) || !((SetPersonalGoal) reply).getReply())
                    throw new NoMessageToReadException();
                else
                    System.out.println(username + " received his personal goal");
            } catch (ClientDisconnectedException cde){
                System.out.println("Client Disconnected after receiving Personal Goal");
            } catch (NoMessageToReadException nme){
                System.out.println("No Personal Goal confirmation was received");
            } catch (InterruptedException ignored){}
            System.out.println(username + " is ready");
        }
        //DISTRIBUTE TILES
        board.refillBoard();
        System.out.println("Game " + gameID + " can now start");
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
        //pick tiles
        //insert tiles
        //check common goals
        //check board
        //check shelf completeness
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
