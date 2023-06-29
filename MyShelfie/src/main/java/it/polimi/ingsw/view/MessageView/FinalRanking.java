package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Pair;

import java.util.ArrayList;
/**class: CommonReached
 * @author Angelo Di Rosa
 * Message class used to communicate the final rankings that needs to be shown onto the final sceen.*/
public class FinalRanking extends MessageView {
    private ArrayList<Pair<String,Integer>> ranks;
    public FinalRanking(ArrayList<Pair<String,Integer>> ranks){
        super(MessageCodeView.SHOW_FINALRANKS);
        this.ranks=ranks;
    }

    public ArrayList<Pair<String,Integer>> getRanks() {
        return ranks;
    }
}
