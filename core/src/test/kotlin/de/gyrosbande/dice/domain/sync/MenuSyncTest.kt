package de.gyrosbande.dice.domain.sync

import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.Drink
import de.gyrosbande.dice.domain.MenuSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MenuSyncTest {

    @Test
    fun `the San Remo seed survives a round trip unchanged`() {
        val decoded = MenuSync.decode(MenuSync.encode(MenuSeed.categories))
        assertEquals(MenuSeed.categories, decoded)
    }

    @Test
    fun `drink order is preserved - it is game relevant for the wrap rule`() {
        val category = Category(
            diceNumber = 3,
            name = "Bitter",
            drinks = listOf(
                Drink("Averna", 250, "2 cl"),
                Drink("Ramazzotti", 250),
                Drink("Underberg", 300, "2 cl"),
            ),
        )
        val decoded = MenuSync.decode(MenuSync.encode(listOf(category)))
        assertEquals(listOf(category), decoded)
    }

    @Test
    fun `umlauts and special characters in names round trip`() {
        val category = Category(
            diceNumber = 1,
            name = "Süßes & \"Spezielles\"",
            drinks = listOf(Drink("Glühwein/Grog", 350, "0,2 l")),
        )
        val decoded = MenuSync.decode(MenuSync.encode(listOf(category)))
        assertEquals(listOf(category), decoded)
    }

    @Test
    fun `a missing size label round trips as null`() {
        val category = Category(1, "X", listOf(Drink("Ouzo", 250, null)))
        val decoded = MenuSync.decode(MenuSync.encode(listOf(category)))
        assertEquals(null, decoded!!.single().drinks.single().sizeLabel)
    }

    @Test
    fun `null blank and garbage decode to null so the watch falls back to the seed`() {
        assertNull(MenuSync.decode(null))
        assertNull(MenuSync.decode(""))
        assertNull(MenuSync.decode("   "))
        assertNull(MenuSync.decode("not json at all"))
        assertNull(MenuSync.decode("{\"unexpected\":true}"))
    }
}
