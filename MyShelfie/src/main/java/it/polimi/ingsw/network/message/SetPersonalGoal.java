package it.polimi.ingsw.network.message;

/**
 * Class: SetPersonalGoal
 * This is a Message subclass used to give a unique personal goal to every player.
 * @author Mattia Lucamarini
 */
public class SetPersonalGoal extends Message {
    private int goalNumber;
    public SetPersonalGoal(int index) {
        super(MessageCode.SET_PERSONAL_GOAL);
        this.goalNumber = index;
    }
    public SetPersonalGoal(){
        super(MessageCode.SET_PERSONAL_GOAL);
    }
    public int getGoalNumber(){
        return this.goalNumber;
    }
}
