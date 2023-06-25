package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**Class PersonalGoalCard
 * @author Angelo Di Rosa
 * This class represents the physical common goal card used in the game.
 * It is like a physical blank card in which we set its value (the personal goal) */

public class PersonalGoalCard extends Card implements Serializable {
    private PersonalGoal goal;
    private int goalIndex;
    static List<PersonalGoal> personalpointer = PersonalGoal.all();

    /** Method: resetGoalDeck
     * @author Shaffaeet Mohammad
     * This method resets the deck so that the constructor starts fishing from a fresh deck again. */
    public static void resetGoalDeck() {
        personalpointer = PersonalGoal.all();
    }

    /** Method: getGoal()
     * @author Angelo Di Rosa
     * This method overrides the superclass method.
     * It is used to choose a random Personal Goal for the game from a personal goal list.*/
    public void generateGoal(){
        Random rand = new Random();
        int t = rand.nextInt(personalpointer.size());
        goal = personalpointer.get(t);
        personalpointer.remove(t);
        goalIndex = t;
    }
    public void generateGoal(int index){
        goal = personalpointer.get(index);
        personalpointer.remove(index);
        goalIndex = index;
    }
    public PersonalGoalCard(PersonalGoal goal){
        this.goal = goal;
    }
    public PersonalGoalCard(){
        generateGoal();
    }
    public PersonalGoalCard(int index){
        generateGoal(index);
    }
    @Override
    public PersonalGoal getGoal(){
        return this.goal;
    }

    public int getGoalIndex() {
        return goalIndex;
    }
}
