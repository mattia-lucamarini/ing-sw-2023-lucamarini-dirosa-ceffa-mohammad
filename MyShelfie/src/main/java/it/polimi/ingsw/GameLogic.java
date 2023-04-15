package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageCode;
import it.polimi.ingsw.server.ClientHandler;

import java.util.HashMap;
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
        this.CommonGoals = new Pair<>(CommonGoal.FourCorners(), CommonGoal.Stairs());
        //check shelves
        //distribute personal goals
        //distribute tiles
        //choose first player
        System.out.println(" done");
    }
    public void playTurn(String player){
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
