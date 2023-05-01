package it.polimi.ingsw.model;
import java.util.List;
import java.util.Random;

/**Class: CommonGoalCard
 * @author Angelo Di Rosa
 * This class represents the physical common goal card used in the game.
 * It is like a physical blank card in which we set its value (the common goal) */

public class CommonGoalCard extends Card {
    private CommonGoal goal;
    private int goalIndex;
    static List<CommonGoal> commonpointer;
/**
 * Method: getGoal()
 * @author Angelo Di Rosa
 * This method overrides the superclass method. It is used to choose a Random common goal for the game from a common goal list.*/
    public CommonGoalCard(int numPlayers){
        Random val = new Random();
        goal = new CommonGoal(null, null);
        commonpointer = CommonGoal.all(numPlayers);
        int t =  val.nextInt(commonpointer.size());
        goal = commonpointer.get(t);
        commonpointer.remove(t);
        goalIndex = t;
    }
    public int getGoalIndex() {
        return goalIndex;
    }

    @Override
    public CommonGoal getGoal(){
        return this.goal;
    }

}
