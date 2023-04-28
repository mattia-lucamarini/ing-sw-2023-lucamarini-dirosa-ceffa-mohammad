package it.polimi.ingsw.model;
import java.util.List;
import java.util.Random;

/**Class: CommonGoalCard
 * @author Angelo Di Rosa
 * This class represents the physical common goal card used in the game.
 * It is like a physical blank card in which we set its value (the common goal) */

public class CommonGoalCard extends Card {
    private CommonGoal goal;
    static List<CommonGoal> commonpointer = CommonGoal.all();
/**
 * Method: getGoal()
 * @author Angelo Di Rosa
 * This method overrides the superclass method. It is used to choose a Random common goal for the game from a common goal list.*/
    public CommonGoalCard(){
        Random val = new Random();
        goal = new CommonGoal(null, null);
        int t =  val.nextInt(commonpointer.size());
        goal = commonpointer.get(t);
        commonpointer.remove(t);
    }

    @Override
    public CommonGoal getGoal(){
        return this.goal;
    }

}
