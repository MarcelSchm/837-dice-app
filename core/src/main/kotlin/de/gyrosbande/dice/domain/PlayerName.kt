package de.gyrosbande.dice.domain

/**
 * Rules for player names. Names are what the order summary shouts across
 * the table, so they are capitalized and have to be unique - with two
 * Marcels in the group, one of them becomes "Marcel S".
 */
object PlayerName {

    /** Trims and capitalizes: " marcel " -> "Marcel". */
    fun normalize(raw: String): String =
        raw.trim().replaceFirstChar(Char::uppercaseChar)

    /** True when [raw] is blank once trimmed. */
    fun isBlank(raw: String): Boolean = raw.trim().isEmpty()

    /** True when an equal name already exists (ignoring case and spacing). */
    fun isTaken(raw: String, existing: Iterable<String>): Boolean {
        val candidate = normalize(raw)
        if (candidate.isEmpty()) return false
        return existing.any { it.trim().equals(candidate, ignoreCase = true) }
    }
}
