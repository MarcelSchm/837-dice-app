package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerNameTest {

    @Test
    fun `capitalizes the first letter`() {
        assertEquals("Marcel", PlayerName.normalize("marcel"))
        assertEquals("Marcel", PlayerName.normalize("Marcel"))
        assertEquals("Marcel S", PlayerName.normalize("marcel S"))
    }

    @Test
    fun `trims surrounding whitespace`() {
        assertEquals("Kevin", PlayerName.normalize("  kevin  "))
        assertEquals("", PlayerName.normalize("   "))
    }

    @Test
    fun `leaves the rest of the name alone`() {
        // No title-casing - "Marcel H" must not become "Marcel h" or similar.
        assertEquals("Marcel H", PlayerName.normalize("marcel H"))
        assertEquals("MARCEL", PlayerName.normalize("MARCEL"))
    }

    @Test
    fun `detects duplicates ignoring case and spacing`() {
        val existing = listOf("Marcel", "Kevin")
        assertTrue(PlayerName.isTaken("marcel", existing))
        assertTrue(PlayerName.isTaken("  MARCEL ", existing))
        assertTrue(PlayerName.isTaken("Kevin", existing))
    }

    @Test
    fun `distinct names are not duplicates`() {
        val existing = listOf("Marcel S", "Kevin")
        assertFalse(PlayerName.isTaken("Marcel H", existing))
        assertFalse(PlayerName.isTaken("Jonas", existing))
        // Blank input is not a duplicate - it is simply rejected elsewhere
        assertFalse(PlayerName.isTaken("   ", existing))
    }

    @Test
    fun `blank detection`() {
        assertTrue(PlayerName.isBlank(""))
        assertTrue(PlayerName.isBlank("   "))
        assertFalse(PlayerName.isBlank(" M "))
    }
}
