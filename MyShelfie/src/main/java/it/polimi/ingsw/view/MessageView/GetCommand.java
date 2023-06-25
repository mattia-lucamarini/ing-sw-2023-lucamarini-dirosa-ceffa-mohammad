package it.polimi.ingsw.view.MessageView;

public class GetCommand extends MessageView{
    private String command;

    public GetCommand(String command){
        super(MessageCodeView.COMMAND);
        this.command=command;
    }
    public String getContent(){
        return command;
    }
}
