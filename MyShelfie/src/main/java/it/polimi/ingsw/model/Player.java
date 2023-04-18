package it.polimi.ingsw.model;

/**
 * Class: Player
 * @author Mohammad Shaffaeet
 * This class is an abstraction of the Player
 * each player contain a personal goalcard
 * each player contain a shelf
 */
public class Player {

    private String username;
    private Shelf shelf;
    private PersonalGoal goalCard;

    public Player(String username){
        this.username = username;
        this.shelf = new Shelf();
    }
    public String getUsername(){
        return this.username;
    }
    public Shelf getShelf(){
        return this.shelf;
    }
    public PersonalGoal getGoal(){
        return goalCard;
    }

}



