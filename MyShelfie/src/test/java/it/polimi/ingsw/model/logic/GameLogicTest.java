package it.polimi.ingsw.model.logic;

import it.polimi.ingsw.model.BoardTest;
import it.polimi.ingsw.model.Pair;
import it.polimi.ingsw.model.Shelf;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.ClientHandler.SocketClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;
import org.junit.Test;
import org.mockito.Mockito.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GameLogicTest {
    public GameLogic gameLogic1() {
        var players = new ConcurrentHashMap<String, ClientHandler>();
        players.put("Marco", mock(ClientHandler.class));
        players.put("Luigi", mock(ClientHandler.class));
        players.put("Giorgio", mock(ClientHandler.class));

        return new GameLogic(players, 0, BoardTest.rulesExBoard(3));
    }

    @Test
    public void testMarcoTake2Then1Tiles() throws NoMessageToReadException, ClientDisconnectedException {
        var players = new ConcurrentHashMap<String, ClientHandler>();

        // Marco takes 2 tiles, the 1 tile, then ends turn.
        var marco = mock(ClientHandler.class);
        when(marco.receivingWithRetry(any(), any())).thenReturn(
                new ChosenTiles(
                        new ArrayList<>(List.of(Pair.of(6, 5), Pair.of(7, 5)))
                ),
                new ChosenTiles(
                        new ArrayList<>(List.of(Pair.of(5, 3)))
                ),
                new Message(MessageCode.TURN_OVER),
                new CommonGoalReached("Marco", 2),
                new FullShelf("Marco", false),
                new Message(MessageCode.TURN_OVER),
                // TODO: Maybe give real shelf to Marco.
                new ShelfCheck(mock(Shelf.class))
        );
        players.put("Marco", marco);

        players.put("Luigi", mock(ClientHandler.class));
        players.put("Giorgio", mock(ClientHandler.class));

        var gameLogic = new GameLogic(players, 0, BoardTest.rulesExBoard(3));

        gameLogic.playTurn("Marco");
    }
}