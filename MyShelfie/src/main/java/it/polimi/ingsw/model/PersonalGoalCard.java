package it.polimi.ingsw.model;

import java.util.List;
import java.util.Random;

/**Class PersonalGoalCard
 * @author Angelo Di Rosa
 * This class represents the physical common goal card used in the game.
 * It is like a physical blank card in which we set its value (the personal goal) */

public class PersonalGoalCard extends Card {
    private PersonalGoal goal;
    List<PersonalGoal> personalpointer;

    /** Method: getGoal()
     * @author Angelo Di Rosa
     * This method overrides the superclass method.
     * It is used to choose a random Personal Goal for the game from a personal goal list.*/
    @Override
    public PersonalGoal getGoal(){
        goal = new PersonalGoal(null, null);
        Random rand = new Random();
        personalpointer = goal.all();
        int t = rand.nextInt(personalpointer.size());
        goal = personalpointer.get(t);

        return goal;
    }
}
