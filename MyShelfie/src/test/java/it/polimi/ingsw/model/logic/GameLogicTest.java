package it.polimi.ingsw.model.logic;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class GameLogicTest {
    public static ClientHandler newClientHMock() {
        var mck = mock(ClientHandler.class);
        when(mck.isConnected()).thenReturn(true);
        return mck;
    }

    public static List<Message> getMessagesFromMockClient(ClientHandler clientH, int wantedNumOfInvocations) throws ClientDisconnectedException {
        var captor = ArgumentCaptor.forClass(Message.class);
        verify(clientH, times(wantedNumOfInvocations)).sendingWithRetry(
                captor.capture(), anyInt(), anyInt()
        );

        return captor.getAllValues();
    }

    @Test
    public void testMarcoTakes2Then1Tiles() throws NoMessageToReadException, ClientDisconnectedException {
        // TODO: this shouldn't pass. Wait for client fix.
        Assert.assertFalse(true);

        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Define Marco's turn. He takes 2 tiles, the 1 tile, then ends turn.
        var marco = newClientHMock();
        var tilesToTake = List.of(Pair.of(6, 5), Pair.of(7, 5), Pair.of(5, 3));
        when(marco.receivingWithRetry(anyInt(), anyInt())).thenReturn(
                new ChosenTiles(tilesToTake.subList(0, 2)),
                new ChosenTiles(tilesToTake.subList(2, 3)),
                new Message(MessageCode.TURN_OVER),
                new CommonGoalReached("Marco", 2), // 2 means no goal reached.
                new FullShelf("Marco", false),
                new Message(MessageCode.TURN_OVER),
                // TODO: Maybe give real shelf to Marco.
                new ShelfCheck(mock(Shelf.class))
        );
        players.put("Marco", marco);

        players.put("Luigi", newClientHMock());
        players.put("Giorgio", newClientHMock());

        // Build GameLogic with example board from rules.
        var gameLogic = new GameLogic(players, 0, BoardTest.rulesExampleBoard(3));

        // Make Marco have 1 turn.
        gameLogic.playTurn("Marco");

        // Check that the correct tiles have been removed.
        var expectedBoard = BoardTest.rulesExampleBoard(3);
        expectedBoard.takeTiles(tilesToTake);
        Assert.assertEquals(expectedBoard, gameLogic.getBoard());
    }

    @Test
    public void testMarcoTakes2IntoEmptyShelf() throws NoMessageToReadException, ClientDisconnectedException {
        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Define Marco's turn: He takes 2 tiles, then 1 tile, then ends turn.
        var marco = new Player("Marco");
        // Create mock of client.
        var marcoClient = newClientHMock();
        var tilesToTake = new ArrayList<>(List.of(Pair.of(6, 5), Pair.of(7, 5)));
        // Messages to send to server in order.
        when(marcoClient.receivingWithRetry(anyInt(), anyInt()))
                // Special message that adds tiles to Marco's shelf.
                .thenAnswer(
                    _invOnMock -> {
                        marco.getShelf().insertTiles(
                                List.of(Pair.of(0, 0), Pair.of(1, 0)),
                                List.of(Tiles.BLUE, Tiles.BLUE)
                        );
                        return new ChosenTiles(tilesToTake);
                    }
                // Other normal messages.
                ).thenReturn(
                new Message(MessageCode.TURN_OVER),
                new CommonGoalReached("Marco", 2), // 2 means no goal reached.
                new FullShelf("Marco", false),
                new Message(MessageCode.TURN_OVER),
                new ShelfCheck(marco.getShelf())
        );
        players.put("Marco", marcoClient);

        players.put("Luigi", newClientHMock());
        players.put("Giorgio", newClientHMock());

        // Build GameLogic with example board from rules.
        var gameLogic = new GameLogic(players, 0, BoardTest.rulesExampleBoard(3));

        // Make Marco have 1 turn.
        gameLogic.playTurn("Marco");

        // Check that other players receive the correct notifications from Marco's turn.
        for (var entry : players.entrySet()) {
            if (entry.getKey().equals("Marco")) continue;

            var msgs = getMessagesFromMockClient(entry.getValue(), 4);
            Assert.assertEquals(msgs.get(0).getMessageType(), MessageCode.PLAY_TURN);
            Assert.assertEquals(msgs.get(1).getMessageType(), MessageCode.CHOSEN_TILES);
            Assert.assertEquals(msgs.get(2).getMessageType(), MessageCode.TURN_OVER);
            Assert.assertEquals(((ShelfCheck) msgs.get(3)).getShelf(), marco.getShelf());
        }

        // Check that the correct tiles have been removed.
        var expectedBoard = BoardTest.rulesExampleBoard(3);
        expectedBoard.takeTiles(tilesToTake);
        Assert.assertEquals(expectedBoard, gameLogic.getBoard());
    }

    @Test
    public void testMarcoCompletesFirstCommonGoal() throws NoMessageToReadException, ClientDisconnectedException {
        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Define Marco's turn: He takes 2 tiles, completes a common goal and ends.
        var marco = new Player("Marco");
        // Create mock of client.
        var marcoClient = newClientHMock();
        // Messages to send to server in order.
        when(marcoClient.receivingWithRetry(anyInt(), anyInt())).thenReturn(
                new Message(MessageCode.TURN_OVER),
                new CommonGoalReached("Marco", 0), // First common goal reached
                new FullShelf("Marco", false),
                new Message(MessageCode.TURN_OVER),
                new ShelfCheck(mock(Shelf.class))
        );
        players.put("Marco", marcoClient);
        players.put("Luigi", newClientHMock());
        players.put("Giorgio", newClientHMock());

        // Build GameLogic with example board from rules.
        var game = new GameLogic(players, 0, BoardTest.rulesExampleBoard(3));
        var scoredPoints = game.getCommonGoals().getFirst().getGoal().peekPoints();

        // Make Marco have 1 turn.
        game.playTurn("Marco");

        // Check that common goal points have been attributed.
        Assert.assertEquals(scoredPoints, game.getPlayerPoints().get("Marco").intValue());

        // Check that other players receive the correct notifications from Marco's turn.
        for (var entry : players.entrySet()) {
            if (entry.getKey().equals("Marco")) continue;

            Assert.assertEquals(0, game.getPlayerPoints().get(entry.getKey()).intValue());

            var msgs = getMessagesFromMockClient(entry.getValue(), 5);
            Assert.assertEquals(MessageCode.PLAY_TURN,           msgs.get(0).getMessageType());
            Assert.assertEquals(MessageCode.CHOSEN_TILES,        msgs.get(1).getMessageType());
            Assert.assertEquals(MessageCode.COMMON_GOAL_REACHED, msgs.get(2).getMessageType());
            var msg = (CommonGoalReached) msgs.get(2);
            Assert.assertEquals(0, msg.getPosition());
            Assert.assertEquals(MessageCode.TURN_OVER,           msgs.get(3).getMessageType());
            Assert.assertEquals(MessageCode.SHELF_CHECK,         msgs.get(4).getMessageType());
        }
    }

    @Test
    public void testMarcoCompletesShelf() throws NoMessageToReadException, ClientDisconnectedException {
        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Define Marco's turn: He takes 2 tiles, completes a common goal and ends.
        var marco = new Player("Marco");
        // Create mock of client.
        var marcoClient = newClientHMock();
        // Messages to send to server in order.
        when(marcoClient.receivingWithRetry(anyInt(), anyInt())).thenReturn(
                new Message(MessageCode.TURN_OVER),
                new CommonGoalReached("Marco", 2), // 2 means no goal reached
                new FullShelf("Marco", true),
                new Message(MessageCode.TURN_OVER),
                new ShelfCheck(mock(Shelf.class))
        );
        players.put("Marco", marcoClient);
        players.put("Luigi", newClientHMock());
        players.put("Giorgio", newClientHMock());

        // Build GameLogic with example board from rules.
        var game = new GameLogic(players, 0, BoardTest.rulesExampleBoard(3));

        // Make Marco have 1 turn.
        game.playTurn("Marco");

        // Check that common goal points have been attributed.
        Assert.assertEquals(1, game.getPlayerPoints().get("Marco").intValue());

        // Check that other players receive the correct notifications from Marco's turn.
        for (var entry : players.entrySet()) {
            if (entry.getKey().equals("Marco")) continue;

            Assert.assertEquals(0, game.getPlayerPoints().get(entry.getKey()).intValue());

            var msgs = getMessagesFromMockClient(entry.getValue(), 5);
            Assert.assertEquals(MessageCode.PLAY_TURN,           msgs.get(0).getMessageType());
            Assert.assertEquals(MessageCode.CHOSEN_TILES,        msgs.get(1).getMessageType());
            Assert.assertEquals(MessageCode.FULL_SHELF, msgs.get(2).getMessageType());
            var msg = (FullShelf) msgs.get(2);
            Assert.assertTrue(msg.getOutcome());
            Assert.assertEquals(MessageCode.TURN_OVER,           msgs.get(3).getMessageType());
            Assert.assertEquals(MessageCode.SHELF_CHECK,         msgs.get(4).getMessageType());
        }
    }

    @Test
    public void testMarcoTakesNoTilesThenDisconnects() throws NoMessageToReadException, ClientDisconnectedException {
        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Define Marco's turn: He takes 2 tiles, completes a common goal and ends.
        var marco = new Player("Marco");
        // Create mock of client.
        var marcoClient = newClientHMock();
        // Messages to send to server in order.
        when(marcoClient.receivingWithRetry(anyInt(), anyInt()))
                .thenReturn(new Message(MessageCode.TURN_OVER))
                .thenThrow(new ClientDisconnectedException());
        players.put("Marco", marcoClient);
        players.put("Luigi", newClientHMock());
        players.put("Giorgio", newClientHMock());

        // Build GameLogic with example board from rules.
        var gameLogic = new GameLogic(players, 0, BoardTest.rulesExampleBoard(3));

        // Make Marco have 1 turn.
        gameLogic.playTurn("Marco");

        // Check that other players receive the correct notifications from Marco's turn.
        for (var entry : players.entrySet()) {
            if (entry.getKey().equals("Marco")) continue;

            var msgs = getMessagesFromMockClient(entry.getValue(), 2);
            Assert.assertEquals(MessageCode.PLAY_TURN,           msgs.get(0).getMessageType());
            Assert.assertEquals(MessageCode.CHOSEN_TILES,        msgs.get(1).getMessageType());
        }
    }

    @Test
    public void testMarcoTakesNoTilesThenNoMsgsInQueue() throws NoMessageToReadException, ClientDisconnectedException {
        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Define Marco's turn: He takes 2 tiles, completes a common goal and ends.
        var marco = new Player("Marco");
        // Create mock of client.
        var marcoClient = newClientHMock();
        // Messages to send to server in order.
        when(marcoClient.receivingWithRetry(anyInt(), anyInt()))
                .thenReturn(new Message(MessageCode.TURN_OVER))
                .thenThrow(new NoMessageToReadException());
        players.put("Marco", marcoClient);
        players.put("Luigi", newClientHMock());
        players.put("Giorgio", newClientHMock());

        // Build GameLogic with example board from rules.
        var gameLogic = new GameLogic(players, 0, BoardTest.rulesExampleBoard(3));

        // Make Marco have 1 turn.
        gameLogic.playTurn("Marco");

        // Check that other players receive the correct notifications from Marco's turn.
        for (var entry : players.entrySet()) {
            if (entry.getKey().equals("Marco")) continue;

            var msgs = getMessagesFromMockClient(entry.getValue(), 2);
            Assert.assertEquals(MessageCode.PLAY_TURN,           msgs.get(0).getMessageType());
            Assert.assertEquals(MessageCode.CHOSEN_TILES,        msgs.get(1).getMessageType());
        }
    }

    @Test
    public void testPersonalGoalsAndGroupsPointsConsistency() throws NoMessageToReadException, ClientDisconnectedException {
        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Create mock of client.
        var marcoShelf = ShelfTest.stairsShelf();
        var marcoClient = newClientHMock();
        // Messages to send to server in order.
        when(marcoClient.receivingWithRetry(anyInt(), anyInt()))
                .thenReturn(new SetPersonalGoal(), new ShelfCheck(marcoShelf));
        players.put("Marco", marcoClient);

        players.put("Luigi", newClientHMock());
        players.put("Giorgio", newClientHMock());
        for (var entry : players.entrySet()) {
            if (entry.getKey().equals("Marco")) continue;
            when(entry.getValue().receivingWithRetry(anyInt(), anyInt()))
                    .thenReturn(new SetPersonalGoal());
        }

        // Build GameLogic with example board from rules.
        var game = new GameLogic(players, 0, BoardTest.rulesExampleBoard(3));

        // Game end + assign points from groups and personal goals to Marco (he has a stairs shelf).
        game.sendGoalsToClients();
        game.assignPoints("Marco");

        // Check that other players receive the correct notifications from Marco's turn.
        var msgs = getMessagesFromMockClient(players.get("Marco"), 3);
        Assert.assertEquals(MessageCode.SET_PERSONAL_GOAL, msgs.get(0).getMessageType());
        var msg = (SetPersonalGoal) msgs.get(0);
        var personalGoalSent = new PersonalGoalCard(msg.getGoalNumber());
        var pgPoints = personalGoalSent.getGoal().checkGoal(marcoShelf);
        int tgPoints = 0;
        for (var group : marcoShelf.findTileGroups()) {
            tgPoints += Shelf.scoreGroup(group);
        }
        Assert.assertEquals(
                pgPoints + tgPoints,
                game.getPlayerPoints().get("Marco").intValue()
        );

        Assert.assertEquals(MessageCode.SET_COMMON_GOALS,  msgs.get(1).getMessageType());
        Assert.assertEquals(MessageCode.END_GAME,          msgs.get(2).getMessageType());
    }
}