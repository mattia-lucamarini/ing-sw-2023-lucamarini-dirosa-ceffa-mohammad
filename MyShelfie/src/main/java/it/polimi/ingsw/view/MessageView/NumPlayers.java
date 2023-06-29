package it.polimi.ingsw.view.MessageView;
/**class: NumPlayer
 * @author Angelo Di Rosa
 * Message class used to communicate the number of players selected from the input*/
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
