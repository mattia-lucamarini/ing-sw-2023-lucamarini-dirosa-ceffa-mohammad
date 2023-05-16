package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Pair;

import java.util.ArrayList;

public class FinalScore extends Message {
    private ArrayList<Pair<String, Integer>> score;
    public FinalScore(ArrayList<Pair<String, Integer>> score){
        super(MessageCode.FINAL_SCORE);
        this.score = score;
    }
    public ArrayList<Pair<String, Integer>> getScore() {
        return score;
    }
}
