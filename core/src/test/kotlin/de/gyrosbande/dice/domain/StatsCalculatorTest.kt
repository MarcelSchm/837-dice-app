package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsCalculatorTest {

    private var uuidCounter = 0

    private fun result(
        player: String,
        drink: String = "Ouzo 38 %",
        category: String = "Schnäpse & Brände",
        priceCents: Int = 250,
        drinkRolls: List<Int> = listOf(3),
        categorySize: Int = 7,
        sizeLabel: String? = "2 cl",
    ) = HistoryResult(
        playerName = player,
        categoryName = category,
        drinkName = drink,
        sizeLabel = sizeLabel,
        priceCents = priceCents,
        categoryRoll = 1,
        drinkRolls = drinkRolls,
        categorySize = categorySize,
        wasVirtual = true,
    )

    private fun round(vararg results: HistoryResult) = HistoryRound(
        uuid = "round-${uuidCounter++}",
        startedAt = 1_000L,
        finishedAt = 2_000L,
        results = results.toList(),
    )

    @Test
    fun `empty history yields empty stats`() {
        val stats = StatsCalculator.calculate(emptyList())
        assertEquals(0, stats.roundCount)
        assertEquals(0, stats.rollCount)
        assertEquals(0, stats.totalCents)
        assertNull(stats.proseccoKing)
        assertNull(stats.topSpender)
        assertNull(stats.favoriteDrink)
        assertNull(stats.categoryMagnet)
        assertNull(stats.doublesChamp)
        assertNull(stats.wrapVictim)
    }

    @Test
    fun `prosecco king counts only the bottle`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(
                    result("Marcel", drink = "Flasche Prosecco", priceCents = 1350, sizeLabel = "0,7 l"),
                    result("Kevin", drink = "Glas Prosecco", priceCents = 290, sizeLabel = "0,1 l"),
                ),
                round(
                    result("Marcel", drink = "Flasche Prosecco", priceCents = 1350, sizeLabel = "0,7 l"),
                    result("Kevin"),
                ),
            )
        )
        assertEquals(listOf("Marcel"), stats.proseccoKing!!.names)
        assertEquals(2, stats.proseccoKing!!.count)
    }

    @Test
    fun `top spender sums prices and reports ties`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(
                    result("Marcel", priceCents = 250),
                    result("Kevin", priceCents = 250),
                    result("Jonas", priceCents = 100),
                ),
            )
        )
        assertEquals(listOf("Kevin", "Marcel"), stats.topSpender!!.names)
        assertEquals(250, stats.topSpender!!.cents)
    }

    @Test
    fun `favorite drink distinguishes glass and bottle`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(
                    result("Marcel", drink = "Glas Prosecco", sizeLabel = "0,1 l"),
                    result("Kevin", drink = "Glas Prosecco", sizeLabel = "0,1 l"),
                    result("Jonas", drink = "Flasche Prosecco", sizeLabel = "0,7 l"),
                ),
            )
        )
        assertEquals("Glas Prosecco", stats.favoriteDrink!!.drinkName)
        assertEquals(2, stats.favoriteDrink!!.count)
    }

    @Test
    fun `category magnet needs at least two hits`() {
        val once = StatsCalculator.calculate(
            listOf(round(result("Marcel", category = "Bitter")))
        )
        assertNull(once.categoryMagnet)

        val twice = StatsCalculator.calculate(
            listOf(
                round(result("Marcel", category = "Bitter"), result("Kevin", category = "Likör")),
                round(result("Marcel", category = "Bitter"), result("Kevin", category = "Bitter")),
            )
        )
        assertEquals(listOf("Marcel"), twice.categoryMagnet!!.names)
        assertEquals("Bitter", twice.categoryMagnet!!.categoryName)
        assertEquals(2, twice.categoryMagnet!!.count)
    }

    @Test
    fun `doubles and wraps are detected from the rolls`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(
                    // double 4+4, sum 8 > 7 drinks -> also a wrap
                    result("Marcel", drinkRolls = listOf(4, 4), categorySize = 7),
                    // 5+3 = 8 > 7 -> wrap, no double
                    result("Kevin", drinkRolls = listOf(5, 3), categorySize = 7),
                    // one die, no double, no wrap
                    result("Jonas", drinkRolls = listOf(2), categorySize = 4),
                ),
                round(
                    // categorySize unknown (old data): never counts as wrap
                    result("Kevin", drinkRolls = listOf(6, 6), categorySize = 0),
                ),
            )
        )
        assertEquals(listOf("Kevin", "Marcel"), stats.doublesChamp!!.names)
        assertEquals(1, stats.doublesChamp!!.count)
        assertEquals(listOf("Kevin", "Marcel"), stats.wrapVictim!!.names)
        assertEquals(1, stats.wrapVictim!!.count)
    }

    @Test
    fun `total includes manually added extras`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(result("Marcel", priceCents = 250)).copy(
                    extras = listOf(ExtraItem("Menü 837", 850, 2)),
                ),
            )
        )
        // 2,50 € drink + 2 x 8,50 € food
        assertEquals(1950, stats.totalCents)
        // but the roll count stays drink-based
        assertEquals(1, stats.rollCount)
    }

    @Test
    fun `totals add up`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(result("Marcel", priceCents = 250), result("Kevin", priceCents = 1350)),
                round(result("Marcel", priceCents = 400)),
            )
        )
        assertEquals(2, stats.roundCount)
        assertEquals(3, stats.rollCount)
        assertEquals(2000, stats.totalCents)
        assertEquals("20,00 €", stats.totalFormatted)
    }

    @Test
    fun `regular and rareGuest with clear attendance difference`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(result("Marcel"), result("Kevin")),
                round(result("Marcel"), result("Kevin")),
                round(result("Marcel"), result("Jonas")),
                round(result("Marcel")),
            )
        )
        assertEquals(listOf("Marcel"), stats.regular!!.names)
        assertEquals(4, stats.regular!!.count)
        assertEquals(listOf("Jonas"), stats.rareGuest!!.names)
        assertEquals(1, stats.rareGuest!!.count)
    }

    @Test
    fun `regular with tie shows all names sorted alphabetically`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(result("Marcel"), result("Kevin")),
                round(result("Marcel"), result("Kevin")),
                round(result("Jonas")),
            )
        )
        assertEquals(listOf("Kevin", "Marcel"), stats.regular!!.names)
        assertEquals(2, stats.regular!!.count)
        assertEquals(listOf("Jonas"), stats.rareGuest!!.names)
        assertEquals(1, stats.rareGuest!!.count)
    }

    @Test
    fun `less than 3 rounds yields null for regular and rareGuest`() {
        val twoRounds = StatsCalculator.calculate(
            listOf(
                round(result("Marcel"), result("Kevin")),
                round(result("Marcel")),
            )
        )
        assertNull(twoRounds.regular)
        assertNull(twoRounds.rareGuest)

        val oneRound = StatsCalculator.calculate(
            listOf(round(result("Marcel")))
        )
        assertNull(oneRound.regular)
        assertNull(oneRound.rareGuest)
    }

    @Test
    fun `all players in all rounds yields no rareGuest`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(result("Marcel"), result("Kevin"), result("Jonas")),
                round(result("Marcel"), result("Kevin"), result("Jonas")),
                round(result("Marcel"), result("Kevin"), result("Jonas")),
            )
        )
        assertEquals(listOf("Jonas", "Kevin", "Marcel"), stats.regular!!.names)
        assertEquals(3, stats.regular!!.count)
        assertNull(stats.rareGuest)
    }

    @Test
    fun `single player yields no rareGuest`() {
        val stats = StatsCalculator.calculate(
            listOf(
                round(result("Marcel")),
                round(result("Marcel")),
                round(result("Marcel")),
            )
        )
        assertEquals(listOf("Marcel"), stats.regular!!.names)
        assertEquals(3, stats.regular!!.count)
        assertNull(stats.rareGuest)
    }

    @Test
    fun `two results in one round still count as a single attendance`() {
        // Reachable once results can be added to a round afterwards. Without
        // counting per round, Marcel would end up "bei 4 von 3 Runden dabei".
        val stats = StatsCalculator.calculate(
            listOf(
                round(result("Marcel"), result("Marcel"), result("Kevin")),
                round(result("Marcel"), result("Kevin")),
                round(result("Marcel")),
            )
        )
        assertEquals(listOf("Marcel"), stats.regular!!.names)
        assertEquals(3, stats.regular!!.count)
        assertEquals(listOf("Kevin"), stats.rareGuest!!.names)
        assertEquals(2, stats.rareGuest!!.count)
    }
}
