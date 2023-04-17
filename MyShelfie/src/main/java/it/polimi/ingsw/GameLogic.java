package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageCode;
import it.polimi.ingsw.network.message.SetPersonalGoal;
import it.polimi.ingsw.server.ClientHandler;

import java.util.*;
import java.util.function.Predicate;

/**
 * Class: GameLogic
 * @author Mattia Lucamarini
 * This class implements the game rules and manages the players' turns.
 */
public class GameLogic implements Runnable{
    private final HashMap<String, ClientHandler> clientList;
    private final int numPlayers;
    private int gameID;
    private boolean isActive;
    private Board board;
    private Bag tiles;
    private Pair<Predicate<Shelf>, Predicate<Shelf>> CommonGoals;
    private List<String> playerOrder;

    public GameLogic(HashMap<String, ClientHandler> clientList, int gameID){
        this.clientList = clientList;
        this.numPlayers = clientList.size();
        this.gameID = gameID;
        this.isActive = true;
    }
    @Override
    public void run(){
        System.out.println("Starting game "+ gameID);
        for (String username : clientList.keySet()){
            boolean status = clientList.get(username).send(new Message(username, MessageCode.GENERIC_MESSAGE));
            if (status){
                System.out.println(username + " is ready");
            }
            else{
                System.out.println(username + " is not ready");
                throw new RuntimeException();
            }
        }
        System.out.print("Preparing game " + gameID + "...");
        this.board = new Board(numPlayers);
        this.tiles = new Bag();
        //extract common goals TODO: random
        // TODO: OLD VERSION: this.CommonGoals = new Pair<>(CommonGoal.FourCorners(), CommonGoal.Stairs());
        //distribute personal goals TODO: random
        for (String username : clientList.keySet()){
            try {
                clientList.get(username)
                        .send(new SetPersonalGoal(username, new PersonalGoal(new HashMap<Pair<Integer, Integer>, Tiles>(), new ArrayList<Integer>())));
            }
            catch (Exception e){
                System.out.println("Couldn't send Personal Goal to " + username);
            }
        }
        //distribute tiles
        board.refillBoard();
        System.out.println(" done");
        //choose first player
        playerOrder = clientList.keySet().stream().toList();
        Collections.shuffle(playerOrder);
        System.out.println("Player order: ");
        for (String pl : playerOrder)
            System.out.println(pl);
        playTurn(playerOrder.get(0));
    }
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
