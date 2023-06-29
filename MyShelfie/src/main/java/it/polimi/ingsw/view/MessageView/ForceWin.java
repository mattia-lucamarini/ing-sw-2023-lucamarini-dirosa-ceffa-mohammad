package it.polimi.ingsw.view.MessageView;

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
