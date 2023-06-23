package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Tiles;
import javafx.scene.image.Image;

public class SelectedTile extends MessageView{
    private Tiles tilevalue;
    private int index1, index2;


    public SelectedTile(int i, int j){
        super(MessageCodeView.SELECTED_TILE);
        this.index1 = i;
        this.index2 = j;
    }

    public Pair<Integer,Integer> getIndexes(){
        return Pair.of(index1,index2);
    }
}
