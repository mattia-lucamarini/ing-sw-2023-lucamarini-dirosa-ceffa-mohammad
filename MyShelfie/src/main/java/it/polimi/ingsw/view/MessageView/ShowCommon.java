package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Pair;
/**class: ShowCommon
 * @author Angelo Di Rosa
 * Message class used to communicate the number of the commongoals so the gui can show the image.
*/
public class ShowCommon extends MessageView{
    private int goal1, goal2;
    public ShowCommon(int goal1, int goal2){
        super(MessageCodeView.COMMON_GOAL);
        this.goal1 = goal1;
        this.goal2 = goal2;
    }

    public Pair<Integer,Integer> getGoalIndex() {
        return new Pair<>(goal1, goal2);
    }
}
