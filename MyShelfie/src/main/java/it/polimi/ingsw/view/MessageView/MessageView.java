package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.network.message.MessageCode;
/**class: MessageView
 * @author Angelo Di Rosa
 * Generic Message Superclass*/
public class MessageView {
    private final MessageCodeView messageType;

    public MessageView(MessageCodeView messageType) {
        this.messageType = messageType;
    }
    public MessageCodeView getType(){
        return messageType;
    }
}
