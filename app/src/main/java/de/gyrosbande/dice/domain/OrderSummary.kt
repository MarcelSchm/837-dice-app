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
 * Groups the outcomes of a round into the order to show the waiter.
 * Drinks are grouped by identity (name + size + price) and keep the order
 * of their first appearance.
 */
object OrderSummary {

    fun lines(outcomes: List<RollOutcome>): List<OrderLine> =
        outcomes
            .map { it.drink }
            .groupBy { it }
            .map { (drink, occurrences) -> OrderLine(drink, occurrences.size) }

    fun totalCents(outcomes: List<RollOutcome>): Int =
        outcomes.sumOf { it.drink.priceCents }

    fun totalFormatted(outcomes: List<RollOutcome>): String {
        val cents = totalCents(outcomes)
        return "%d,%02d €".format(cents / 100, cents % 100)
    }
}
