package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameFlowTest {

    @Test
    fun `complete round with manual entry`() {
        val flow = GameFlow()
        assertEquals(RollPhase.CategoryRoll, flow.phase)
        assertEquals(1, flow.requiredDice())

        // roll 1: a 3 -> Bitter
        flow.enterManual(listOf(3))
        val drinkPhase = flow.phase as RollPhase.DrinkRoll
        assertEquals("Bitter", drinkPhase.category.name)
        assertEquals(1, flow.requiredDice())

        // roll 2: a 3 -> Jägermeister
        flow.enterManual(listOf(3))
        val done = flow.phase as RollPhase.Finished
        assertEquals("Jägermeister 35 %", done.outcome.drink.name)
        assertEquals(3, done.outcome.categoryRoll)
        assertEquals(listOf(3), done.outcome.drinkRolls)
    }

    @Test
    fun `schnaps category needs two dice and wraps`() {
        val flow = GameFlow()
        flow.enterManual(listOf(1)) // Schnäpse & Brände (7 drinks)
        assertEquals(2, flow.requiredDice())

        flow.enterManual(listOf(4, 5)) // sum 9 -> wrap -> Grappa
        val done = flow.phase as RollPhase.Finished
        assertEquals("Grappa 40 %", done.outcome.drink.name)
        assertEquals(9, done.outcome.drinkRollTotal)
    }

    @Test
    fun `wrong dice count is rejected`() {
        val flow = GameFlow()
        assertThrows(IllegalArgumentException::class.java) {
            flow.enterManual(listOf(1, 2)) // category roll needs exactly 1
        }
        assertThrows(IllegalArgumentException::class.java) {
            flow.enterManual(listOf(7)) // not a valid pip value
        }
    }

    @Test
    fun `virtual round yields a valid outcome`() {
        // Fixed seed keeps the test deterministic.
        val flow = GameFlow(random = Random(837))
        val categoryDice = flow.rollVirtual()
        assertEquals(1, categoryDice.size)
        assertTrue(categoryDice.single() in 1..6)

        val drinkDice = flow.rollVirtual()
        assertTrue(drinkDice.all { it in 1..6 })

        val done = flow.phase as RollPhase.Finished
        assertTrue(done.outcome.category.drinks.contains(done.outcome.drink))
    }

    @Test
    fun `reset starts a new round`() {
        val flow = GameFlow()
        flow.enterManual(listOf(2))
        flow.enterManual(listOf(1))
        assertTrue(flow.phase is RollPhase.Finished)

        flow.reset()
        assertEquals(RollPhase.CategoryRoll, flow.phase)
    }

    @Test
    fun `every menu category is reachable and playable`() {
        for (n in 1..6) {
            val flow = GameFlow()
            flow.enterManual(listOf(n))
            val phase = flow.phase as RollPhase.DrinkRoll
            // every possible roll must point to a drink (wrap rule)
            for (roll in DiceRules.rollRange(phase.diceCount)) {
                DiceRules.drinkFor(phase.category, roll)
            }
        }
    }
}
