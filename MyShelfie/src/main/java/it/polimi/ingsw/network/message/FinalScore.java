package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;

import java.util.ArrayList;

/**
 * Contains an ArrayList carrying the final score.
 * @author Mattia Lucamarini
 */
public class FinalScore extends Message {
    private ArrayList<Pair<String, Integer>> score;

    /**
     * @param score pair of username and score
     */
    public FinalScore(ArrayList<Pair<String, Integer>> score){
        super(MessageCode.FINAL_SCORE);
        this.score = score;
    }
    public ArrayList<Pair<String, Integer>> getScore() {
        return score;
    }
}
