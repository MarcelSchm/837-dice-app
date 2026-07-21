package de.gyrosbande.dice.domain.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class RoundSyncTest {

    @Test
    fun `a full round state round trips`() {
        val state = WatchRoundState(
            active = true,
            currentPlayer = "Marcel S",
            playerIndex = 1,
            totalPlayers = 4,
            stage = RoundStage.RESULT,
            rolling = false,
            category = "Bitter",
            resultDrink = "Averna",
            resultPrice = "2,50 €",
        )
        assertEquals(state, RoundSync.decode(RoundSync.encode(state)))
    }

    @Test
    fun `garbage and blank decode to the inactive state`() {
        assertEquals(WatchRoundState.INACTIVE, RoundSync.decode(null))
        assertEquals(WatchRoundState.INACTIVE, RoundSync.decode(""))
        assertEquals(WatchRoundState.INACTIVE, RoundSync.decode("not json"))
    }
}
