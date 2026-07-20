package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DiceRulesTest {

    @Test
    fun `up to 6 drinks one die is enough`() {
        assertEquals(1, DiceRules.diceCountFor(1))
        assertEquals(1, DiceRules.diceCountFor(4))
        assertEquals(1, DiceRules.diceCountFor(6))
    }

    @Test
    fun `7 or more drinks require two dice`() {
        assertEquals(2, DiceRules.diceCountFor(7))
        assertEquals(2, DiceRules.diceCountFor(12))
    }

    @Test
    fun `value range for one and two dice`() {
        assertEquals(1..6, DiceRules.rollRange(1))
        assertEquals(2..12, DiceRules.rollRange(2))
    }

    @Test
    fun `roll within the list hits directly`() {
        // 4 bitters, roll 3 -> Jägermeister (index 2)
        assertEquals(2, DiceRules.resolveIndex(roll = 3, itemCount = 4))
        // roll exactly on the last entry
        assertEquals(3, DiceRules.resolveIndex(roll = 4, itemCount = 4))
    }

    @Test
    fun `wrap rule - off the bottom back to the top`() {
        // Example from CLAUDE.md: 7 schnapps, sum 9 -> 9-7=2 -> index 1 (Grappa)
        assertEquals(1, DiceRules.resolveIndex(roll = 9, itemCount = 7))
        // maximum roll 12 with 7 drinks -> 12-7=5 -> index 4
        assertEquals(4, DiceRules.resolveIndex(roll = 12, itemCount = 7))
        // 4 drinks, roll 5 -> back to the top: index 0
        assertEquals(0, DiceRules.resolveIndex(roll = 5, itemCount = 4))
        // double wrap: 3 drinks, roll 7 -> 7-3-3=1 -> index 0
        assertEquals(0, DiceRules.resolveIndex(roll = 7, itemCount = 3))
    }

    @Test
    fun `wrap example with real menu data`() {
        val schnaps = MenuSeed.categoryFor(1)
        assertEquals("Grappa 40 %", DiceRules.drinkFor(schnaps, 9).name)
        assertEquals("Ouzo 38 %", DiceRules.drinkFor(schnaps, 8).name)
        assertEquals("Wodka 38 %", DiceRules.drinkFor(schnaps, 7).name)

        val rumSpecial = MenuSeed.categoryFor(2)
        assertEquals("Flasche Prosecco", DiceRules.drinkFor(rumSpecial, 2).name)
        // 5 drinks, roll 6 -> wraps to the top -> Bacardi
        assertEquals("Bacardi 37,5 %", DiceRules.drinkFor(rumSpecial, 6).name)
    }

    @Test
    fun `invalid input throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            DiceRules.resolveIndex(roll = 0, itemCount = 4)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DiceRules.resolveIndex(roll = 3, itemCount = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            DiceRules.diceCountFor(0)
        }
    }
}
