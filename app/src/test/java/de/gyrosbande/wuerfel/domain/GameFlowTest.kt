package de.gyrosbande.wuerfel.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameFlowTest {

    @Test
    fun `kompletter Durchgang mit manueller Eingabe`() {
        val flow = GameFlow()
        assertEquals(RollPhase.CategoryRoll, flow.phase)
        assertEquals(1, flow.requiredDice())

        // Wurf 1: eine 3 → Bitter
        flow.enterManual(listOf(3))
        val drinkPhase = flow.phase as RollPhase.DrinkRoll
        assertEquals("Bitter", drinkPhase.category.name)
        assertEquals(1, flow.requiredDice())

        // Wurf 2: eine 3 → Jägermeister
        flow.enterManual(listOf(3))
        val done = flow.phase as RollPhase.Finished
        assertEquals("Jägermeister 35 %", done.outcome.drink.name)
        assertEquals(3, done.outcome.categoryRoll)
        assertEquals(listOf(3), done.outcome.drinkRolls)
    }

    @Test
    fun `Schnaepse brauchen zwei Wuerfel und wrappen`() {
        val flow = GameFlow()
        flow.enterManual(listOf(1)) // Schnäpse & Brände (7 Drinks)
        assertEquals(2, flow.requiredDice())

        flow.enterManual(listOf(4, 5)) // Summe 9 → Wrap → Grappa
        val done = flow.phase as RollPhase.Finished
        assertEquals("Grappa 40 %", done.outcome.drink.name)
        assertEquals(9, done.outcome.drinkRollTotal)
    }

    @Test
    fun `falsche Wuerfelanzahl wird abgelehnt`() {
        val flow = GameFlow()
        assertThrows(IllegalArgumentException::class.java) {
            flow.enterManual(listOf(1, 2)) // Kategorie-Wurf braucht genau 1
        }
        assertThrows(IllegalArgumentException::class.java) {
            flow.enterManual(listOf(7)) // keine gültige Augenzahl
        }
    }

    @Test
    fun `virtueller Durchgang liefert gueltiges Ergebnis`() {
        // Fester Seed macht den Test deterministisch.
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
    fun `reset startet neuen Durchgang`() {
        val flow = GameFlow()
        flow.enterManual(listOf(2))
        flow.enterManual(listOf(1))
        assertTrue(flow.phase is RollPhase.Finished)

        flow.reset()
        assertEquals(RollPhase.CategoryRoll, flow.phase)
    }

    @Test
    fun `jede Kategorie der Karte ist erreichbar und bespielbar`() {
        for (n in 1..6) {
            val flow = GameFlow()
            flow.enterManual(listOf(n))
            val phase = flow.phase as RollPhase.DrinkRoll
            // Jeder mögliche Wurf muss auf einen Drink zeigen (Wrap-Regel)
            for (roll in DiceRules.rollRange(phase.diceCount)) {
                DiceRules.drinkFor(phase.category, roll)
            }
        }
    }
}
