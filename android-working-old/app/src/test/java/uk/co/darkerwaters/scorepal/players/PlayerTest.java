package uk.co.darkerwaters.scorepal.players;

import org.junit.Test;

import uk.co.darkerwaters.scorepal.players.Player;

import static org.junit.Assert.assertEquals;

public class PlayerTest {

    @Test
    public void playerName() {
        Player playerOne = new Player(0, "playerOne");

        assertEquals("player name", "playerOne", playerOne.getName());
        playerOne.setName("player1");
        assertEquals("player name", "player1", playerOne.getName());
    }

    @Test
    public void serving() {
        Player playerOne = new Player(0, "playerOne");

        assertEquals("player serving", false, playerOne.getIsServing());
        playerOne.setIsServing(true);
        assertEquals("player serving", true, playerOne.getIsServing());
    }

    @Test
    public void resetting() {
        Player playerOne = new Player(0, "playerOne");

        assertEquals("player serving", false, playerOne.getIsServing());
        playerOne.setIsServing(true);
        assertEquals("player serving", true, playerOne.getIsServing());
    }
}