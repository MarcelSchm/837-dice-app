package de.gyrosbande.dice.domain

import java.util.Calendar

/**
 * One player's result inside a finished round, as recorded at roll time
 * (snapshot - immune to later menu edits).
 */
data class HistoryResult(
    val playerName: String,
    val categoryName: String,
    val drinkName: String,
    val sizeLabel: String?,
    val priceCents: Int,
    val categoryRoll: Int,
    val drinkRolls: List<Int>,
    /** Drinks in the category at roll time; 0 = unknown (pre-v2 data). */
    val categorySize: Int,
    /** True when the rolled drink was unavailable and replaced by hand. */
    val substituted: Boolean = false,
    val wasVirtual: Boolean,
) {
    val drink: Drink get() = Drink(drinkName, priceCents, sizeLabel)

    val isDouble: Boolean
        get() = drinkRolls.size == 2 && drinkRolls[0] == drinkRolls[1]

    /** True when the wrap rule kicked in ("off the bottom, back to the top"). */
    val isWrap: Boolean
        get() = categorySize > 0 && drinkRolls.sum() > categorySize
}

/** A finished round as shown in the history. */
data class HistoryRound(
    val uuid: String,
    val startedAt: Long,
    val finishedAt: Long,
    val results: List<HistoryResult>,
) {
    val totalCents: Int get() = results.sumOf { it.priceCents }

    val year: Int
        get() = Calendar.getInstance().apply { timeInMillis = startedAt }.get(Calendar.YEAR)
}
