package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderSummaryTest {

    private fun outcome(category: Category, drink: Drink) =
        RollOutcome(category, drink, categoryRoll = category.diceNumber, drinkRolls = listOf(1))

    private val bitter = MenuSeed.categoryFor(3)
    private val rumSpecial = MenuSeed.categoryFor(2)
    private val ouzoCategory = MenuSeed.categoryFor(1)

    @Test
    fun `same drinks are grouped with quantities`() {
        val ouzo = ouzoCategory.drinks[0] // 2,50 €
        val prosecco = rumSpecial.drinks[1] // Flasche Prosecco, 13,50 €
        val outcomes = listOf(
            outcome(ouzoCategory, ouzo),
            outcome(rumSpecial, prosecco),
            outcome(ouzoCategory, ouzo),
        )

        val lines = OrderSummary.lines(outcomes)
        assertEquals(2, lines.size)
        // first appearance keeps its position
        assertEquals("Ouzo 38 %", lines[0].drink.name)
        assertEquals(2, lines[0].quantity)
        assertEquals(500, lines[0].totalCents)
        assertEquals("5,00 €", lines[0].totalFormatted)
        assertEquals(1, lines[1].quantity)
    }

    @Test
    fun `total sums all drinks including duplicates`() {
        val jaeger = bitter.drinks[2] // 2,50 €
        val prosecco = rumSpecial.drinks[1] // 13,50 €
        val outcomes = listOf(
            outcome(bitter, jaeger),
            outcome(bitter, jaeger),
            outcome(rumSpecial, prosecco),
        )

        assertEquals(1850, OrderSummary.totalCents(outcomes))
        assertEquals("18,50 €", OrderSummary.totalFormatted(outcomes))
    }

    @Test
    fun `empty round has an empty order`() {
        assertTrue(OrderSummary.lines(emptyList()).isEmpty())
        assertEquals(0, OrderSummary.totalCents(emptyList()))
    }
}
