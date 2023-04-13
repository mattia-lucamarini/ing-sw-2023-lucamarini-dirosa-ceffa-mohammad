package it.polimi.ingsw;

import java.util.function.Predicate;

// Da considerre se spostare i predicati quì per avere le cose più ordinate.
public class CommonGoalPredicates {
    static Predicate<Shelf> Adjacent(int nOfGroups, int nOfTiles) {
        return (Shelf shelf) -> {
            // Count how many groups of "nOfTiles" tiles are there.
            int count = 0;
            for (var group : shelf.findTileGroups()) {
                if (group.getSecond() == nOfTiles) count++;
            }

            // Constraint is reached if they reach "nOfTiles" (or more).
            return count >= nOfGroups;
        };
    }
}
