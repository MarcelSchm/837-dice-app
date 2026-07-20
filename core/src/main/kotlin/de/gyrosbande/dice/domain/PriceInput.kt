package de.gyrosbande.dice.domain

/**
 * Parses German-style price input from the menu editor ("2,50", "13", also
 * "2.50") into cents, and formats cents back for editing.
 */
object PriceInput {

    /** Returns null for anything that is not a sane, non-negative price. */
    fun parseCents(text: String): Int? {
        val normalized = text.trim().replace("€", "").trim().replace(',', '.')
        if (normalized.isEmpty()) return null
        val match = Regex("""^(\d{1,4})(?:\.(\d{0,2}))?$""").find(normalized) ?: return null
        val euros = match.groupValues[1].toInt()
        val centsPart = match.groupValues[2].padEnd(2, '0')
        val cents = if (centsPart.isEmpty()) 0 else centsPart.toInt()
        return euros * 100 + cents
    }

    /** Formats cents for an input field: 250 -> "2,50". */
    fun format(cents: Int): String = "%d,%02d".format(cents / 100, cents % 100)
}
