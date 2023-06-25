package it.polimi.ingsw.network.message;

/**
 * Class: SetPersonalGoal
 * This is a Message subclass used to give a unique personal goal to every player.
 * @author Mattia Lucamarini
 */
public class SetPersonalGoal extends Message {
    private int goalNumber;
    private final boolean reply;
    public SetPersonalGoal(int index) {
        super(MessageCode.SET_PERSONAL_GOAL);
        this.goalNumber = index;
        this.reply = false;
    }
    public SetPersonalGoal(){
        super(MessageCode.SET_PERSONAL_GOAL);
        this.reply = true;
    }
    public int getGoalNumber(){
        return this.goalNumber;
    }
    public boolean getReply() { return this.reply; }
}
