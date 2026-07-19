package de.gyrosbande.dice.domain

/**
 * A drink from the San Remo menu.
 *
 * @param priceCents price in cents to avoid rounding errors.
 * @param sizeLabel e.g. "2 cl", "0,7 l" - only if printed on the menu.
 */
data class Drink(
    val name: String,
    val priceCents: Int,
    val sizeLabel: String? = null,
) {
    val priceFormatted: String
        get() = "%d,%02d €".format(priceCents / 100, priceCents % 100)
}

/**
 * A dice category. [diceNumber] is the pip count of the category roll.
 * The order of [drinks] matches the menu top to bottom - it determines
 * which drink belongs to which rolled number (wrap rule).
 */
data class Category(
    val diceNumber: Int,
    val name: String,
    val drinks: List<Drink>,
)
