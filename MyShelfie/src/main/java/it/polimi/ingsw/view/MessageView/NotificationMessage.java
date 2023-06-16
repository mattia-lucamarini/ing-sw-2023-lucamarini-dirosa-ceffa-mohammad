package it.polimi.ingsw.view.MessageView;

public class NotificationMessage extends MessageView{
    public String msg;
    public NotificationMessage(String msg){
        super(MessageCodeView.GENERIC_MESSAGE);
        this.msg = msg;
    }

    public String getText(){
        return msg;
    }
}
