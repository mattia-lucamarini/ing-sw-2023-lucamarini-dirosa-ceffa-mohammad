package it.polimi.ingsw.view.MessageView;

public class CommonReached extends MessageView{
    private int points, numgoal;

    public CommonReached(int points, int numgoal){
        super(MessageCodeView.COMMON_REACHED);
        this.points = points;
        this.numgoal = numgoal;
    }

    public int getPoints() {
        return points;
    }
    public int getNumgoal(){
        return numgoal;
    }
}
