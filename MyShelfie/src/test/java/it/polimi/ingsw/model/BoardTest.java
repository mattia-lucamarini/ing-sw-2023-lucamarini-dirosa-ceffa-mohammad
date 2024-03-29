package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class BoardTest {
    public static Board rulesExampleBoard(int numplayers) {
        return new Board(numplayers, List.of(
                'I', 'I', 'I', '3', '4', 'I', 'I', 'I', 'I',
                'I', 'I', 'I', 'V', 'V', '4', 'I', 'I', 'I',
                'I', 'I', 'B', 'Y', 'B', 'L', '3', 'I', 'I',
                'I', 'B', 'P', 'G', 'Y', 'Y', 'L', 'V', '3',
                'P', 'P', 'G', 'Y', 'W', 'L', 'W', 'V', '4',
                '3', 'V', 'V', 'W', 'P', 'L', 'V', '4', 'I',
                'I', 'I', '3', 'V', 'Y', 'B', '3', 'I', 'I',
                'I', 'I', 'I', 'G', 'L', 'B', 'I', 'I', 'I',
                'I', 'I', 'I', 'I', '4', '3', 'I', 'I', 'I'
        ));
    }

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
    public void testRemoveAlternatingRowsAndColsThenRefill() {
        // Make full board for 4 players.
        Board board = new Board(4);
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());

        // Remove tiles.
        board.printBoard();
        for (int r = 0; r < 9; r += 2) {
            for (int c = 0; c < 9; c++) {
                if (!List.of(Tiles.NOTVALID, Tiles.VALID).contains(board.getTile(r, c)))
                    board.takeTiles(List.of(Pair.of(r, c)));
            }
        }
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c += 2) {
                if (!List.of(Tiles.NOTVALID, Tiles.VALID).contains(board.getTile(r, c)))
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
    public void testCannotGetSurroundedTile() {
        // Make full board for 4 players.
        Board board = new Board(4);
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());

        board.printBoard();

        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(5, 4)))
        );
    }

    @Test
    public void testCannotGetTilesUnusedFor4Players() {
        // Make full board for 4 players.
        Board board = new Board(4);
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());

        board.printBoard();

        // Simple, just take one outside.
        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(0, 0)))
        );

        // Take multiple in different directions not including valid.
        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(0, 0), Pair.of(0, 1), Pair.of(0, 2)))
        );
        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(0, 0), Pair.of(1, 0), Pair.of(2, 0)))
        );

        // Take multiple in different directions including valid.
        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(0, 1), Pair.of(0, 2), Pair.of(0, 3)))
        );
        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(3, 0), Pair.of(4, 0), Pair.of(5, 0)))
        );

        // Make moves along first 3 columns (only 4h and 5th rows are valid).
        for (int r = 0; r < 9; r++) {
            final int row = r;
            if (r == 4 || r == 5) continue;
            Assert.assertThrows(
                    RuntimeException.class,
                    () -> board.takeTiles(List.of(Pair.of(row, 0), Pair.of(row, 1), Pair.of(row, 2)))
            );
        }
    }

    @Test
    public void testCannotGetTilesUnusedForUnder4Players() {
        // Make full board for 4 players.
        Board board = new Board(3);
        board.refillBoard();
        Assert.assertFalse(board.checkStatus());

        board.printBoard();

        // Simple, just take one outside.
        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(0, 0)))
        );
        Assert.assertThrows(
                RuntimeException.class,
                () -> board.takeTiles(List.of(Pair.of(0, 4)))
        );

        // Make moves along first 3 columns (only 5th rows are valid).
        for (int r = 0; r < 9; r++) {
            final int row = r;
            if (r == 4) continue;
            Assert.assertThrows(
                    RuntimeException.class,
                    () -> board.takeTiles(List.of(Pair.of(row, 0), Pair.of(row, 1), Pair.of(row, 2)))
            );
        }
    }
}