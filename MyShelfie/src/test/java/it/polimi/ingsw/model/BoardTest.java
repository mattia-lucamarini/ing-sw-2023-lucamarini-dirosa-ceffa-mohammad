package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class BoardTest {
    @Test
    public void testNoRefillAfterFirstMove() {
        // Make full board for 4 players.
        Board board = new Board(4);
        Assert.assertFalse(board.checkStatus());

        // Make a valid move.
        board.takeTiles(List.of(Pair.of(0, 3), Pair.of(0, 4)));

        // Board still no ready for refill.
        Assert.assertFalse(board.checkStatus());
    }
}
