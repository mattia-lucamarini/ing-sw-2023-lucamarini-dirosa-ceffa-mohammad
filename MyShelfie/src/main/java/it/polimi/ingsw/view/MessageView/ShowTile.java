package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Shelf;
import it.polimi.ingsw.model.Tiles;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class ShowTile extends MessageView{
    private Shelf shelf;
    public ShowTile(Shelf shelf){
        super(MessageCodeView.SHOW_TILE);
        this.shelf = shelf;
    }

    public Shelf getShelf() {
        return shelf;
    }
}
