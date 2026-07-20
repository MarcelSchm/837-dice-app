package de.gyrosbande.dice.domain

/** Winners of a count-based fun fact; [names] holds everyone who ties. */
data class TopEntry(val names: List<String>, val count: Int)

/** Winner of the money-based fun fact. */
data class MoneyEntry(val names: List<String>, val cents: Int)

/** The group's most-rolled drink. */
data class DrinkEntry(val drinkName: String, val sizeLabel: String?, val count: Int)

/** Player(s) who keep landing in the same category. */
data class CategoryMagnet(val names: List<String>, val categoryName: String, val count: Int)

/**
 * The hall-of-fame numbers, computed from finished rounds. Fields are null
 * when there is no data for them yet (the UI hides those cards).
 */
data class Stats(
    val roundCount: Int,
    val rollCount: Int,
    val totalCents: Int,
    /** Who got the bottle of Prosecco most often. */
    val proseccoKing: TopEntry?,
    /** Who racked up the highest bill overall. */
    val topSpender: MoneyEntry?,
    /** The group's most-rolled drink. */
    val favoriteDrink: DrinkEntry?,
    /** Player + category combination with the most hits. */
    val categoryMagnet: CategoryMagnet?,
    /** Most doubles on the two-dice drink roll. */
    val doublesChamp: TopEntry?,
    /** Most rolls where the wrap rule kicked in. */
    val wrapVictim: TopEntry?,
) {
    val totalFormatted: String
        get() = "%d,%02d €".format(totalCents / 100, totalCents % 100)
}

/** Pure statistics over the round history - fully unit-testable. */
object StatsCalculator {

    /** Drink name on the San Remo menu that crowns the Prosecco king. */
    const val PROSECCO_BOTTLE = "Flasche Prosecco"

    fun calculate(rounds: List<HistoryRound>): Stats {
        val results = rounds.flatMap { it.results }

        return Stats(
            roundCount = rounds.size,
            rollCount = results.size,
            totalCents = results.sumOf { it.priceCents },
            proseccoKing = topByCount(results.filter { it.drinkName == PROSECCO_BOTTLE }),
            topSpender = topSpender(results),
            favoriteDrink = favoriteDrink(results),
            categoryMagnet = categoryMagnet(results),
            doublesChamp = topByCount(results.filter { it.isDouble }),
            wrapVictim = topByCount(results.filter { it.isWrap }),
        )
    }

    /** Counts [results] per player and returns the leader(s), or null. */
    private fun topByCount(results: List<HistoryResult>): TopEntry? {
        val counts = results.groupingBy { it.playerName }.eachCount()
        val max = counts.values.maxOrNull() ?: return null
        return TopEntry(
            names = counts.filterValues { it == max }.keys.sorted(),
            count = max,
        )
    }

    private fun topSpender(results: List<HistoryResult>): MoneyEntry? {
        val spent = results
            .groupBy { it.playerName }
            .mapValues { (_, r) -> r.sumOf { it.priceCents } }
        val max = spent.values.maxOrNull() ?: return null
        if (max == 0) return null
        return MoneyEntry(
            names = spent.filterValues { it == max }.keys.sorted(),
            cents = max,
        )
    }

    private fun favoriteDrink(results: List<HistoryResult>): DrinkEntry? {
        val counts = results.groupingBy { it.drinkName to it.sizeLabel }.eachCount()
        val (drink, count) = counts.maxByOrNull { it.value } ?: return null
        return DrinkEntry(drinkName = drink.first, sizeLabel = drink.second, count = count)
    }

    private fun categoryMagnet(results: List<HistoryResult>): CategoryMagnet? {
        val counts = results.groupingBy { it.playerName to it.categoryName }.eachCount()
        val max = counts.values.maxOrNull() ?: return null
        // Only fun once someone visibly "collects" a category.
        if (max < 2) return null
        val leaders = counts.filterValues { it == max }.keys
        val category = leaders.first().second
        return CategoryMagnet(
            names = leaders.filter { it.second == category }.map { it.first }.sorted(),
            categoryName = category,
            count = max,
        )
    }
}
