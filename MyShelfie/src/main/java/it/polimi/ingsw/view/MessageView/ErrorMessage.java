package it.polimi.ingsw.view.MessageView;

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
