package it.polimi.ingsw.view.MessageView;
/**class: LabelChange
 * @author Angelo Di Rosa
 * Message class used to update the label on the command panel */
public class LabelChange extends MessageView{
    public String msg;
    public LabelChange(String msg){
        super(MessageCodeView.LABEL_CHANGE);
        this.msg = msg;
    }

    public String getText(){
        return msg;
    }
}
