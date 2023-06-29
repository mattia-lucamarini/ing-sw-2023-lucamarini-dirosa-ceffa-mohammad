package it.polimi.ingsw.view.MessageView;
/**class: ErrorMessage
 * @author Angelo Di Rosa
 * Message class used to communicate that an error occurred and needs to be shown.*/
public class ErrorMessage extends MessageView{
    private String label;

    public ErrorMessage(String label){
        super(MessageCodeView.ERROR_MESSAGE);
        this.label=label;
    }
    public String getLabel(){
        return label;
    }
}
