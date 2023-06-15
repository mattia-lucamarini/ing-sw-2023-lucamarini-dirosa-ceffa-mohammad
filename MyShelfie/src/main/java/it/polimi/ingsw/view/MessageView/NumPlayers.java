package it.polimi.ingsw.view.MessageView;

public class NumPlayers extends MessageView{
    private int numplayers;
    public NumPlayers(int numplayers){
        super(MessageCodeView.GENERIC_MESSAGE);
        this.numplayers=numplayers;
    }
    public int getNumplayers(){
        return numplayers;
    }
}
