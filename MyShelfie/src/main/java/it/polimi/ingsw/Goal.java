package it.polimi.ingsw;

public interface Goal {
    Object getConstraint();
    int checkGoal(Shelf shelf);
}
