package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.network.message.MessageCode;

public class PayloadUsername extends MessageView{
    private String username;
    private String connection;
    private int port;
    private String address;

    public PayloadUsername(String username, String connection, String address, int port){
        super(MessageCodeView.GENERIC_MESSAGE);
        this.username=username;
        this.connection=connection;
        this.address = address;
        this.port= port;
    }

    public String getUsername() {
        return username;
    }

    public String getConnection() {
        return connection;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
