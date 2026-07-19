package de.gyrosbande.wuerfel.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DiceRulesTest {

    @Test
    fun `bis 6 Drinks reicht ein Wuerfel`() {
        assertEquals(1, DiceRules.diceCountFor(1))
        assertEquals(1, DiceRules.diceCountFor(4))
        assertEquals(1, DiceRules.diceCountFor(6))
    }

    @Test
    fun `ab 7 Drinks braucht es zwei Wuerfel`() {
        assertEquals(2, DiceRules.diceCountFor(7))
        assertEquals(2, DiceRules.diceCountFor(12))
    }

    @Test
    fun `Wertebereich fuer ein und zwei Wuerfel`() {
        assertEquals(1..6, DiceRules.rollRange(1))
        assertEquals(2..12, DiceRules.rollRange(2))
    }

    @Test
    fun `Wurf innerhalb der Liste trifft direkt`() {
        // 4 Bitter, Wurf 3 → Jägermeister (Index 2)
        assertEquals(2, DiceRules.resolveIndex(roll = 3, itemCount = 4))
        // Wurf genau auf den letzten Eintrag
        assertEquals(3, DiceRules.resolveIndex(roll = 4, itemCount = 4))
    }

    @Test
    fun `Wrap-Regel - unten durch oben weiter`() {
        // Beispiel aus CLAUDE.md: 7 Schnäpse, Summe 9 → 9-7=2 → Index 1 (Grappa)
        assertEquals(1, DiceRules.resolveIndex(roll = 9, itemCount = 7))
        // Maximalwurf 12 bei 7 Drinks → 12-7=5 → Index 4
        assertEquals(4, DiceRules.resolveIndex(roll = 12, itemCount = 7))
        // 4 Drinks, Wurf 5 → wieder oben: Index 0
        assertEquals(0, DiceRules.resolveIndex(roll = 5, itemCount = 4))
        // Doppelter Wrap: 3 Drinks, Wurf 7 → 7-3-3=1 → Index 0
        assertEquals(0, DiceRules.resolveIndex(roll = 7, itemCount = 3))
    }

    @Test
    fun `Wrap-Beispiel mit echten Kartendaten`() {
        val schnaepse = MenuSeed.categoryFor(1)
        assertEquals("Grappa 40 %", DiceRules.drinkFor(schnaepse, 9).name)
        assertEquals("Ouzo 38 %", DiceRules.drinkFor(schnaepse, 8).name)
        assertEquals("Wodka 38 %", DiceRules.drinkFor(schnaepse, 7).name)

        val rumSpezial = MenuSeed.categoryFor(2)
        assertEquals("Flasche Prosecco", DiceRules.drinkFor(rumSpezial, 2).name)
        // 5 Drinks, Wurf 6 → oben weiter → Bacardi
        assertEquals("Bacardi 37,5 %", DiceRules.drinkFor(rumSpezial, 6).name)
    }

    @Test
    fun `ungueltige Eingaben werfen Fehler`() {
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
