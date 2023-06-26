package it.polimi.ingsw.view.MessageView;

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
