package it.polimi.ingsw.client;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.ClientHandler.ClientHandler;
import it.polimi.ingsw.network.message.*;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.NoMessageToReadException;
import it.polimi.ingsw.view.CLIInterface;
import it.polimi.ingsw.view.UserInterface;
import javafx.scene.effect.Reflection;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.reflection.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class ClientTest {
    @Test
    public void testPlayTurnTake2TilesAndInsert() throws NoMessageToReadException, ClientDisconnectedException {
        // Prepare static fields of client.

        // Client's player.
        ReflectionTestUtils.setField(Client.class, "player", new Player("Marco"));

        // Mock client's common goals to always fail (we don't care for this test).
        var comGoalMock = mock(CommonGoal.class);
        var comGoalCMock = mock(CommonGoalCard.class);
        when(comGoalMock.checkGoal(any(Shelf.class))).thenReturn(0);
        when(comGoalCMock.getGoal()).thenReturn(comGoalMock);
        ReflectionTestUtils.setField(Client.class, "commonGoals", Pair.of(comGoalCMock, comGoalCMock));

        // Mock player UI to simulate a turn in which 2 tiles are taken and inserted into the shelf.
        var uiMock = mock(UserInterface.class);
        when(uiMock.getCommand("Marco"))
                .thenReturn("take", "insert", "done");
        var tilesToInsert = new ArrayList<>(List.of(Tiles.PURPLE, Tiles.PURPLE));
        when(uiMock.takeCommand())
                .thenReturn(tilesToInsert);
        when(uiMock.doneCommand())
                .thenReturn(true);
        ReflectionTestUtils.setField(Client.class, "userInterface", uiMock);

        // Mock client handler to send fake messages from server and respond correctly to client.
        var clientHMock = mock(ClientHandler.class);
        when(clientHMock.receivingWithRetry(anyInt(), anyInt()))
                .thenReturn(
                        // After this msg, mock UI is interrogated for commands (see above).
                        new PlayTurn("Marco", BoardTest.rulesExampleBoard(3)),
                        // Consequent msgs are all awks to make client exit turn smoothly.
                        new CommonGoalReached("Marco", 0),
                        new FullShelf("Marco", false),
                        new Message(MessageCode.TURN_OVER)
                );
        ReflectionTestUtils.setField(Client.class, "clientHandler", clientHMock);

        // Start player turn (invoke method to test).
        ReflectionTestUtils.invokeMethod(Client.class, "playTurn");

        // Check that tiles have been inserted through mock user interface.
        var captor = ArgumentCaptor.forClass(ArrayList.class);
        verify(uiMock, times(1)).insertCommand(captor.capture());
        Assert.assertEquals(tilesToInsert, captor.getAllValues().get(0));
    }
}
