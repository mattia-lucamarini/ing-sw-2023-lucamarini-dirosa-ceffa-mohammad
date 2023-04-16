package it.polimi.ingsw.network.message;

import it.polimi.ingsw.model.PersonalGoal;

/**
 * Class: SetPersonalGoal
 * This is a Message subclass used to give a unique personal goal to every player.
 * @author Mattia Lucamarini
 */
public class SetPersonalGoal extends Message {
    private PersonalGoal goal;
    public SetPersonalGoal(String username, PersonalGoal pg) {
        super(username, MessageCode.SET_PERSONAL_GOAL);
        this.goal = pg;
    }
    public PersonalGoal getPersonalGoal(){
        return this.goal;
    }
}
