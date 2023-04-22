package it.polimi.ingsw.model;

/**
 * Class: Card
 * @author Angelo Di Rosa
 * this class is an abstraction of the (Common/Personal)Cards used in the game.
 * This class is like a blank card in which, when the game starts, we set its value(a predicate from a common or personal goal list)*/
public abstract class Card {
    Goal goal;

    public Goal getGoal(){
        return goal;
    }
}
