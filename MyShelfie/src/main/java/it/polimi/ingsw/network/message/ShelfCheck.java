package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.Shelf;

public class ShelfCheck extends Message {
    private Shelf shelf;
    public ShelfCheck(Shelf shelf){
        super(MessageCode.SHELF_CHECK);
        this.shelf = shelf;
    }

    public Shelf getShelf() {
        return shelf;
    }
}
