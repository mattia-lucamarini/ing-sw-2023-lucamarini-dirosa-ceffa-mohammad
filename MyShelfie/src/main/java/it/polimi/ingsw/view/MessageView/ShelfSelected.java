package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Tiles;

public class ShelfSelected extends MessageView{
    private int index1, index2;


    public ShelfSelected(int i, int j){
        super(MessageCodeView.SHELF);
        this.index1 = i;
        this.index2 = j;
    }

    public Pair<Integer,Integer> getIndexes(){
        return Pair.of(index1,index2);
    }
}
