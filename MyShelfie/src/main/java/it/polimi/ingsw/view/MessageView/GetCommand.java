package it.polimi.ingsw.view.MessageView;
/**class: GetCommand
 * @author Angelo Di Rosa
 * Message class used to communicate the player's command.*/
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
