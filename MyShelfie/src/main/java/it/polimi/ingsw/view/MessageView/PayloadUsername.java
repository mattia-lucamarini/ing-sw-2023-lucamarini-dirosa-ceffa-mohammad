package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.network.message.MessageCode;

public class PayloadUsername extends MessageView{
    private String username;
    private String connection;

    public PayloadUsername(String username, String connection){
        super(MessageCodeView.GENERIC_MESSAGE);
        this.username=username;
        this.connection=connection;
    }

    public String getUsername() {
        return username;
    }

    public String getConnection() {
        return connection;
    }
}
