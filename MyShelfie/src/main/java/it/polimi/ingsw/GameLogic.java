package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.SetPersonalGoal;

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
    private final int gameID;
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
        System.out.println("\nPreparing game " + gameID);
        this.board = new Board(numPlayers);
        this.tiles = new Bag();
        //extract common goals TODO: random
        // TODO: OLD VERSION: this.CommonGoals = new Pair<>(CommonGoal.FourCorners(), CommonGoal.Stairs());
        //distribute personal goals TODO: random
        for (String username : clientList.keySet()){
            try {
                clientList.get(username)
                        .send(new SetPersonalGoal(new PersonalGoal(new HashMap<Pair<Integer, Integer>, Tiles>(), new ArrayList<Integer>())));
                System.out.println(username + " is ready");
            }
            catch (Exception e){
                System.out.println("Couldn't send Personal Goal to " + username);
            }
        }
        //distribute tiles
        board.refillBoard();
        System.out.println("Game " + gameID + " can now start");
        //choose first player
        playerOrder = new ArrayList<>(clientList.keySet().stream().toList());
        Collections.shuffle(playerOrder);
        System.out.print("\nPlayer order for game "+gameID+": ");
        for (String pl : playerOrder)
            System.out.print(pl+" ");
        System.out.println("");
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
