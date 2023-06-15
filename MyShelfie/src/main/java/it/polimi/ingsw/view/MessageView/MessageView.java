package it.polimi.ingsw.view.MessageView;

import it.polimi.ingsw.network.message.MessageCode;

public class MessageView {
    private final MessageCodeView messageType;

    public MessageView(MessageCodeView messageType) {
        this.messageType = messageType;
    }
    public MessageCodeView getType(){
        return messageType;
    }
}
