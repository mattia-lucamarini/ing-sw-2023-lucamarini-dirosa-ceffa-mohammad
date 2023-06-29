package it.polimi.ingsw.view.MessageView;
/**class: NotificationMessage
 * @author Angelo Di Rosa
 * Message class used to communicate a message that will be shown in a popup*/
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
