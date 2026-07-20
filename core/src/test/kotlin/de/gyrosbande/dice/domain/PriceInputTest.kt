package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceInputTest {

    @Test
    fun `parses German comma prices`() {
        assertEquals(250, PriceInput.parseCents("2,50"))
        assertEquals(1350, PriceInput.parseCents("13,50"))
        assertEquals(300, PriceInput.parseCents("3,00"))
    }

    @Test
    fun `parses dot and whole-euro input`() {
        assertEquals(250, PriceInput.parseCents("2.50"))
        assertEquals(1300, PriceInput.parseCents("13"))
        assertEquals(250, PriceInput.parseCents(" 2,5 "))
        assertEquals(200, PriceInput.parseCents("2,"))
        assertEquals(450, PriceInput.parseCents("4,50 €"))
    }

    @Test
    fun `rejects garbage`() {
        assertNull(PriceInput.parseCents(""))
        assertNull(PriceInput.parseCents("   "))
        assertNull(PriceInput.parseCents("abc"))
        assertNull(PriceInput.parseCents("2,505"))
        assertNull(PriceInput.parseCents("-3"))
        assertNull(PriceInput.parseCents("1.2.3"))
        assertNull(PriceInput.parseCents("99999"))
    }

    @Test
    fun `formats cents for editing`() {
        assertEquals("2,50", PriceInput.format(250))
        assertEquals("13,50", PriceInput.format(1350))
        assertEquals("0,00", PriceInput.format(0))
    }

    @Test
    fun `format and parse roundtrip`() {
        for (cents in listOf(0, 1, 99, 100, 250, 1350, 999_999 / 100)) {
            assertEquals(cents, PriceInput.parseCents(PriceInput.format(cents)))
        }
    }
}
