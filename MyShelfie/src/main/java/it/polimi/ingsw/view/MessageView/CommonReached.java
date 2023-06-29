package it.polimi.ingsw.view.MessageView;
/**class: CommonReached
 * @author Angelo Di Rosa
 * Message class used to communicate who reached the commongoal and how many points they gained.*/
public class CommonReached extends MessageView{
    private int points, numgoal;
    private String username;
    public CommonReached(int points, int numgoal, String username){
        super(MessageCodeView.COMMON_REACHED);
        this.points = points;
        this.numgoal = numgoal;
        this.username=username;
    }

    public int getPoints() {
        return points;
    }
    public int getNumgoal(){
        return numgoal;
    }
    public String getUsername() {
        return username;
    }
}
