package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Tiles;

import java.util.List;

public class ShowPickedTiles extends MessageView {
    private List<Tiles> pickedTiles;
    public ShowPickedTiles(List<Tiles> pickedTiles){
        super(MessageCodeView.PICKED_TILES);
        this.pickedTiles = pickedTiles;
    }

    public List<Tiles> getPickedTiles() {
        return pickedTiles;
    }
}
