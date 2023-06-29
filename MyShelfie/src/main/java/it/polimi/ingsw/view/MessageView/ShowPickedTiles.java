package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Tiles;

import java.util.List;
/**class: PickedTiles
 * @author Angelo Di Rosa
 * Message class used to communicate the selected tiles from the board so the GUI can highlight them*/
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
