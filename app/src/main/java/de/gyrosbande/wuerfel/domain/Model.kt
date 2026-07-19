package de.gyrosbande.wuerfel.domain

/**
 * Ein Drink von der San-Remo-Karte.
 *
 * @param priceCents Preis in Cent, damit keine Rundungsfehler entstehen.
 * @param sizeLabel z. B. "2 cl", "0,7 l" – nur wenn es auf der Karte steht.
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
 * Eine Würfel-Kategorie. [diceNumber] ist die Augenzahl im Kategorie-Wurf.
 * Die Reihenfolge von [drinks] entspricht der Karte von oben nach unten –
 * sie bestimmt, welcher Drink zu welcher Wurfzahl gehört.
 */
data class Category(
    val diceNumber: Int,
    val name: String,
    val drinks: List<Drink>,
)
