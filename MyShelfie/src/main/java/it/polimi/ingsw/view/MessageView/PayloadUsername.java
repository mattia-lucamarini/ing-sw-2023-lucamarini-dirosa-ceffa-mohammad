package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.network.message.MessageCode;
/**class: PayloadUsername
 * @author Angelo Di Rosa
 * Message class used to communicate to the GraphicLogic all the credentials in input. */
public class PayloadUsername extends MessageView{
    private String username;
    private String connection;
    private Integer port;
    private String address;

    public PayloadUsername(String username, String connection, String address, Integer port){
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

    public Integer getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
