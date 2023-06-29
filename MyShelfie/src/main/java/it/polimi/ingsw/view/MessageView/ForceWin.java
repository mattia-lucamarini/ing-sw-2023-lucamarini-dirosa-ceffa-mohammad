package it.polimi.ingsw.view.MessageView;
/**class: ForceWin
 * @author Angelo Di Rosa
 * Message class used to communicate that all of the player's but one disconnected and the only player
 * connected will be the winner*/
public class ForceWin extends MessageView{
    public String username;

    public ForceWin(String username) {
        super(MessageCodeView.FORCE_WIN);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
