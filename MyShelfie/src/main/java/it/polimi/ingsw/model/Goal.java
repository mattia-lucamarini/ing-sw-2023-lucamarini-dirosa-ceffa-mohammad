package it.polimi.ingsw.model;

public interface Goal {
    Object getConstraint();
    int checkGoal(Shelf shelf);
}
