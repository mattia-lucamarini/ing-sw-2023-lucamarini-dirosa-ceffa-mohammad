package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.model.Board;

public class UpdateBoard extends MessageView{
    private Board board;
    public UpdateBoard(Board board){
        super(MessageCodeView.UPDATE_BOARD);
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }
}

