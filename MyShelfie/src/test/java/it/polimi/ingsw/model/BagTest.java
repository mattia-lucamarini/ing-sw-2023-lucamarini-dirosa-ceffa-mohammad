package it.polimi.ingsw.model;

import org.junit.Assert;
import org.junit.Test;

public class BagTest {
    @Test
    public void testRemainingTiles() {
        Bag bag = new Bag();

        Assert.assertEquals(Tiles.BLUE, bag.remainingTiles(0));
        Assert.assertEquals(Tiles.GREEN, bag.remainingTiles(1));
        Assert.assertEquals(Tiles.PURPLE, bag.remainingTiles(2));
        Assert.assertEquals(Tiles.YELLOW, bag.remainingTiles(3));

        // Take out all the blue tiles.
        for (int i = 0; i < 21; i++) {
            Assert.assertEquals(Tiles.BLUE, bag.remainingTiles(0));
        }

        // Next tile isn't there.
        Assert.assertEquals(Tiles.NOTVALID, bag.remainingTiles(0));
    }
}
