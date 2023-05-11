package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class BoardTest {
    @Test
    public void testNoRefillAfterFirstMove() {
        // Make full board for 4 players.
        Board board = new Board(4);
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());

        // Make a valid move.
        board.takeTiles(List.of(Pair.of(0, 3), Pair.of(0, 4)));

        // Board still no ready for refill.
        Assert.assertFalse(board.checkStatus());
    }

    @Test
    public void testRemoveAlternatingTilesThenRefill() {
        // Make full board for 4 players.
        Board board = new Board(4);
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());

        // Remove tiles while alternating rows and columns to isolate them all.
        for (int r = 0; r < 9; r++) {
            for (int c = r % 2; c < 9; c += 2) {
                if (board.getTile(r, c) != Tiles.NOTVALID)
                    board.takeTiles(List.of(Pair.of(r, c)));
            }
        }
        board.printBoard();

        // Board ready for refill.
        Assert.assertTrue(board.checkStatus());

        // Refill. Then board not ready again.
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());
    }

    @Test
    public void testRemoveAlternatingRowsAndColsThenRefill() {
        // Make full board for 4 players.
        Board board = new Board(4);
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());

        // Remove tiles.
        for (int r = 0; r < 9; r += 2) {
            for (int c = 0; c < 9; c++) {
                if (board.getTile(r, c) != Tiles.NOTVALID)
                    board.takeTiles(List.of(Pair.of(r, c)));
            }
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c += 2) {
                if (board.getTile(r, c) != Tiles.NOTVALID)
                    board.takeTiles(List.of(Pair.of(r, c)));
            }
        }
        board.printBoard();

        // Board ready for refill.
        Assert.assertTrue(board.checkStatus());

        // Refill. Then board not ready again.
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());


    }
}