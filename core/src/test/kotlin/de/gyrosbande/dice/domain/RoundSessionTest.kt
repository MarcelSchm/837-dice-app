package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundSessionTest {

    private val marcel = Player(1, "Marcel")
    private val kevin = Player(2, "Kevin")

    private fun outcomeFor(categoryRoll: Int, drinkRoll: Int): RollOutcome {
        val flow = GameFlow()
        flow.enterManual(listOf(categoryRoll))
        val dice = if (flow.requiredDice() == 2) listOf(drinkRoll, drinkRoll) else listOf(drinkRoll)
        flow.enterManual(dice)
        return (flow.phase as RollPhase.Finished).outcome
    }

    @Test
    fun `players roll in order and results are collected`() {
        val session = RoundSession(listOf(marcel, kevin))
        assertEquals(marcel, session.currentPlayer)
        assertFalse(session.isFinished)

        session.record(outcomeFor(categoryRoll = 3, drinkRoll = 3))
        assertEquals(kevin, session.currentPlayer)

        session.record(outcomeFor(categoryRoll = 4, drinkRoll = 1))
        assertTrue(session.isFinished)
        assertNull(session.currentPlayer)

        assertEquals(listOf(marcel, kevin), session.results.map { it.player })
        assertEquals("Jägermeister 35 %", session.results[0].outcome.drink.name)
    }

    @Test
    fun `recording after the round is finished fails`() {
        val session = RoundSession(listOf(marcel))
        session.record(outcomeFor(categoryRoll = 3, drinkRoll = 1))
        assertThrows(IllegalStateException::class.java) {
            session.record(outcomeFor(categoryRoll = 3, drinkRoll = 2))
        }
    }

    @Test
    fun `a round without players is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            RoundSession(emptyList())
        }
    }
}
