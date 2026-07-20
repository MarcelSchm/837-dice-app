package de.gyrosbande.dice.domain

/** One line of the grouped order ("2x Ouzo ... 5,00 EUR"). */
data class OrderLine(
    val drink: Drink,
    val quantity: Int,
) {
    val totalCents: Int get() = drink.priceCents * quantity
    val totalFormatted: String
        get() = "%d,%02d €".format(totalCents / 100, totalCents % 100)
}

/**
 * Groups drinks into the order to show the waiter. Drinks are grouped by
 * identity (name + size + price) and keep the order of their first
 * appearance. Works for live rounds ([lines]) and history snapshots
 * ([linesOfDrinks]).
 */
object OrderSummary {

    fun lines(outcomes: List<RollOutcome>): List<OrderLine> =
        linesOfDrinks(outcomes.map { it.drink })

    fun linesOfDrinks(drinks: List<Drink>): List<OrderLine> =
        drinks
            .groupBy { it }
            .map { (drink, occurrences) -> OrderLine(drink, occurrences.size) }

    fun totalCents(outcomes: List<RollOutcome>): Int =
        outcomes.sumOf { it.drink.priceCents }

    fun totalCentsOfDrinks(drinks: List<Drink>): Int =
        drinks.sumOf { it.priceCents }

    fun totalFormatted(outcomes: List<RollOutcome>): String =
        formatCents(totalCents(outcomes))

    fun formatCents(cents: Int): String = "%d,%02d €".format(cents / 100, cents % 100)
}
